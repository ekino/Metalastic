package com.ekino.oss.metalastic.processor.report

import com.ekino.oss.metalastic.processor.CoreConstants
import com.ekino.oss.metalastic.processor.options.ProcessorOptions
import com.google.devtools.ksp.processing.KSPLogger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Processing phases for Metalastic annotation processor. */
enum class ProcessingPhase {
  COLLECTING, // GraphBuilder.collectQClasses() + buildGraphWithoutFields()
  BUILDING, // BuildingOrchestrator - KotlinPoet generation
  WRITING, // writeGeneratedFiles() - file I/O
}

/** Log levels for Reporter messages. */
enum class ReporterLevel {
  DEBUG,
  EXCEPTION,
}

/** A logged message with metadata. */
data class LoggedMessage(
  val level: ReporterLevel,
  val message: String,
  val timestamp: LocalDateTime,
  val phase: ProcessingPhase? = null,
)

/** A report section representing one compilation run */
data class ReportSection(val timestamp: LocalDateTime, val loggedMessages: List<LoggedMessage>)

/** Constants for report formatting */
private object ReportConstants {
  const val CONSOLE_WIDTH = 80
  const val LOG_SEPARATOR_WIDTH = 40
  const val CLEANUP_THRESHOLD_MINUTES = 10L
}

/** Constants for report parsing */
private object ParsingConstants {
  const val REPORT_HEADER_PREFIX = "## report-"
  const val TABLE_SEPARATOR = " | "
  const val TABLE_PREFIX = "| "
  const val LOG_PREFIX = "["
  const val LOG_SEPARATOR = "] "
  const val DURATION_KEYWORD = "Duration:"
  const val MS_SUFFIX = "ms"
  const val PHASE_KEYWORD = "Phase"
  const val TOTAL_KEYWORD = "TOTAL"
  const val SECTION_SEPARATOR = "---"
  const val MIN_TABLE_COLUMNS = 3
}

/**
 * Debug-focused reporter for Metalastic annotation processor.
 *
 * Features:
 * - Lambda-based reporting for lazy evaluation
 * - Console output section for copyable debug logs
 * - Markdown report generation to build/reports/qelasticsearch
 * - Phase timing statistics
 * - Disabled by default, enabled for debugging
 */
interface Reporter {
  // Lambda-based logging levels - only execute when enabled
  fun debug(message: () -> String)

  fun exception(exception: Throwable, message: () -> String)

  // Report generation (if enabled)
  fun generateReport(): String? // Markdown report content

  fun writeReport(): Path? // Write report to file and return path
}

/** Reporter implementation that collects logs and generates both console and markdown output. */
class DefaultReporter(options: ProcessorOptions, private val kspLogger: KSPLogger) : Reporter {

  private val loggedMessages = mutableListOf<LoggedMessage>()
  private var currentPhase: ProcessingPhase? = null
  private var reportWritten: Boolean = false // Prevent duplicate reports

  private val reportPath: Path? =
    options.reportingPath?.let { Paths.get(System.getProperty("user.dir"), it) }

  override fun debug(message: () -> String) {
    val msg = message()
    logMessage(ReporterLevel.DEBUG, msg)
  }

  override fun exception(exception: Throwable, message: () -> String) {
    val msg = message()
    val fullMessage = "$msg: ${exception.message}\nStackTrace:\n${exception.stackTraceToString()}"
    logMessage(ReporterLevel.EXCEPTION, fullMessage)
  }

  override fun generateReport(): String {
    return buildString {
      appendLine("# ${CoreConstants.PRODUCT_NAME} Processor Report")
      appendLine()
      appendLine(
        "Generated: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}"
      )
      appendLine()

      appendLine("## 📋 Detailed Log")
      appendLine()
      appendLine("```")
      loggedMessages.forEach { logMsg -> appendLine(formatLogMessage(logMsg)) }
      appendLine("```")
    }
  }

  override fun writeReport(): Path? {
    if (reportWritten || reportPath == null) return null

    return runCatching {
        // Create parent directories if they don't exist
        Files.createDirectories(reportPath!!.parent)

        // Generate new report content
        val newReportContent = generateCurrentReport()

        // Append to existing file or create new one with TOC
        appendToReport(reportPath, newReportContent)

        reportWritten = true // Mark as written to prevent duplicates
        kspLogger.warn("📄 Report appended to: $reportPath")
        reportPath
      }
      .onFailure { exception ->
        kspLogger.warn("Failed to write report to $reportPath: ${exception.message}")
        kspLogger.exception(exception)
      }
      .getOrNull()
  }

  private fun logMessage(level: ReporterLevel, message: String) {
    loggedMessages.add(
      LoggedMessage(
        level = level,
        message = message,
        timestamp = LocalDateTime.now(),
        phase = currentPhase,
      )
    )
  }

  /** Format a logged message for display */
  private fun formatLogMessage(logMsg: LoggedMessage): String {
    val timestamp = logMsg.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
    val phaseInfo = logMsg.phase?.let { "[${it.name}] " } ?: ""
    val levelIcon =
      when (logMsg.level) {
        ReporterLevel.DEBUG -> "🔍"
        ReporterLevel.EXCEPTION -> "❌"
      }
    return "[$timestamp] $phaseInfo$levelIcon ${logMsg.level}: ${logMsg.message}"
  }

  /** Generate content for the current compilation report */
  private fun generateCurrentReport(): ReportSection {
    val timestamp = LocalDateTime.now()

    return ReportSection(timestamp = timestamp, loggedMessages = loggedMessages.toList())
  }

  /** Append new report section to file, updating TOC */
  private fun appendToReport(filePath: Path, newSection: ReportSection) {
    // Check if file should be cleared based on last modification time
    val shouldClearFile = shouldClearReportFile(filePath)

    val existingContent =
      if (Files.exists(filePath) && !shouldClearFile) {
        Files.readString(filePath)
      } else {
        if (shouldClearFile) {
          logMessage(
            ReporterLevel.DEBUG,
            "Clearing old report file (last modified > ${ReportConstants.CLEANUP_THRESHOLD_MINUTES} minutes ago)",
          )
        }
        ""
      }

    val updatedContent =
      if (existingContent.isBlank()) {
        // Create new file with header and first report
        generateCompleteReport(listOf(newSection))
      } else {
        // Parse existing content and add new section
        val existingSections = parseExistingReports(existingContent)
        val allSections = existingSections + newSection
        generateCompleteReport(allSections)
      }

    Files.writeString(filePath, updatedContent)
  }

  /** Check if report file should be cleared based on last modification time */
  private fun shouldClearReportFile(filePath: Path): Boolean {
    if (!Files.exists(filePath)) return false

    return runCatching {
        val lastModified = Files.getLastModifiedTime(filePath)
        val now = FileTime.from(Instant.now())
        val timeDifference = java.time.Duration.between(lastModified.toInstant(), now.toInstant())
        timeDifference.toMinutes() >= ReportConstants.CLEANUP_THRESHOLD_MINUTES
      }
      .getOrElse { false } // If we can't determine file time, don't clear
  }

  /** Parse existing report file to extract previous report sections */
  private fun parseExistingReports(content: String): List<ReportSection> {
    if (content.isBlank()) return emptyList()

    val reportSections = mutableListOf<ReportSection>()
    val lines = content.lines()
    var i = 0

    while (i < lines.size) {
      val line = lines[i]
      // Look for report section headers: ## report-X---timestamp
      if (line.startsWith("## report-") && line.contains("---")) {
        val (reportSection, nextIndex) = parseReportSection(lines, i)
        if (reportSection != null) {
          reportSections.add(reportSection)
        }
        i = nextIndex
      } else {
        i++
      }
    }

    return reportSections
  }

  /** Extension function to extract timestamp from report section header line */
  private fun String.extractTimestamp(): LocalDateTime? {
    val timestampRegex = """report-\d+---(\d{4}-\d{2}-\d{2}-\d{6})""".toRegex()
    val timestampMatch = timestampRegex.find(this) ?: return null
    val timestampStr = timestampMatch.groupValues[1]

    return runCatching {
        LocalDateTime.parse(timestampStr, DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss"))
      }
      .getOrNull()
  }

  /** Extension function to find the end index of the current report section */
  private fun List<String>.findSectionEndIndex(startIndex: Int): Int {
    for (i in (startIndex + 1) until this.size) {
      val line = this[i]
      if (
        line.startsWith(ParsingConstants.REPORT_HEADER_PREFIX) ||
          line == ParsingConstants.SECTION_SEPARATOR
      ) {
        return i
      }
    }
    return this.size
  }

  /** Extension function to extract duration from a line containing "Duration: XXXms" */
  private fun String.extractDuration(): Long? {
    if (
      !this.contains(ParsingConstants.DURATION_KEYWORD) ||
        !this.contains(ParsingConstants.MS_SUFFIX)
    ) {
      return null
    }
    val durationRegex = """Duration:\s*(\d+)ms""".toRegex()
    val durationMatch = durationRegex.find(this)
    return durationMatch?.groupValues?.get(1)?.toLongOrNull()
  }

  /** Extension function to extract phase timing from a table row */
  private fun String.extractPhaseTimingFromTableRow(): Pair<ProcessingPhase, Long>? {
    return when {
      !this.startsWith(ParsingConstants.TABLE_PREFIX) ||
        !this.contains(ParsingConstants.TABLE_SEPARATOR) ||
        this.contains(ParsingConstants.PHASE_KEYWORD) -> null

      else -> {
        val parts = this.split("|").map { it.trim() }
        when {
          parts.size < ParsingConstants.MIN_TABLE_COLUMNS -> null
          else -> {
            val phaseName = parts[1]
            val durationStr = parts[2].replace("**", "")

            when {
              phaseName == ParsingConstants.TOTAL_KEYWORD || phaseName.isBlank() -> null
              else ->
                runCatching {
                    val phase = ProcessingPhase.valueOf(phaseName)
                    val phaseDuration = durationStr.toLongOrNull()
                    phaseDuration?.let { phase to it }
                  }
                  .getOrNull()
            }
          }
        }
      }
    }
  }

  /** Extension function to extract log message from a log line */
  private fun String.extractLogMessage(baseDate: LocalDateTime): LoggedMessage? {
    return when {
      !this.startsWith(ParsingConstants.LOG_PREFIX) ||
        !this.contains(ParsingConstants.LOG_SEPARATOR) -> null
      else ->
        runCatching {
            // Try DEBUG pattern first
            val debugRegex = """\[(\d{2}:\d{2}:\d{2}\.\d{3})].*?🔍 DEBUG: (.*)""".toRegex()
            val debugMatch = debugRegex.find(this)

            if (debugMatch != null) {
              val timeStr = debugMatch.groupValues[1]
              val message = debugMatch.groupValues[2]
              val logTime =
                LocalDateTime.of(
                  baseDate.toLocalDate(),
                  java.time.LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm:ss.SSS")),
                )

              LoggedMessage(level = ReporterLevel.DEBUG, message = message, timestamp = logTime)
            } else {
              // Try EXCEPTION pattern
              val exceptionRegex = """\[(\d{2}:\d{2}:\d{2}\.\d{3})].*?❌ EXCEPTION: (.*)""".toRegex()
              val exceptionMatch = exceptionRegex.find(this)

              exceptionMatch?.let {
                val timeStr = it.groupValues[1]
                val message = it.groupValues[2]
                val logTime =
                  LocalDateTime.of(
                    baseDate.toLocalDate(),
                    java.time.LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm:ss.SSS")),
                  )

                LoggedMessage(
                  level = ReporterLevel.EXCEPTION,
                  message = message,
                  timestamp = logTime,
                )
              }
            }
          }
          .getOrNull()
    }
  }

  /** Extract multi-line log message starting from current line */
  private fun String.extractMultiLineLogMessage(
    lines: List<String>,
    currentIndex: Int,
    endIndex: Int,
    baseDate: LocalDateTime,
  ): Pair<LoggedMessage?, Int> {
    // First check if current line is a log message
    val singleLineMessage = this.extractLogMessage(baseDate) ?: return null to currentIndex + 1

    // Check if there are continuation lines (lines that don't start with timestamp)
    val messageParts = mutableListOf(singleLineMessage.message)
    var nextIndex = currentIndex + 1

    while (nextIndex < endIndex && nextIndex < lines.size) {
      val nextLine = lines[nextIndex]

      // If the next line starts with a timestamp or is empty, stop
      if (
        nextLine.startsWith(ParsingConstants.LOG_PREFIX) ||
          nextLine.trim().isEmpty() ||
          nextLine.startsWith("```")
      ) {
        break
      }

      // Add continuation line to message
      messageParts.add(nextLine)
      nextIndex++
    }

    // Reconstruct the complete message
    val completeMessage = messageParts.joinToString("\n")
    val multiLineLogMessage =
      LoggedMessage(
        level = singleLineMessage.level,
        message = completeMessage,
        timestamp = singleLineMessage.timestamp,
      )

    return multiLineLogMessage to nextIndex
  }

  /** Extract content data from lines within a report section */
  private fun extractSectionContent(
    lines: List<String>,
    startIndex: Int,
    endIndex: Int,
    timestamp: LocalDateTime,
  ): Triple<Long, Map<ProcessingPhase, Long>, List<LoggedMessage>> {
    var duration = 0L
    val phaseTimings = mutableMapOf<ProcessingPhase, Long>()
    val loggedMessages = mutableListOf<LoggedMessage>()

    var i = startIndex + 1
    while (i < endIndex) {
      val line = lines[i]

      // Extract duration
      line.extractDuration()?.let { duration = it }

      // Extract phase timings
      line.extractPhaseTimingFromTableRow()?.let { (phase, timing) -> phaseTimings[phase] = timing }

      // Extract log messages (may span multiple lines)
      val (logMessage, nextIndex) = line.extractMultiLineLogMessage(lines, i, endIndex, timestamp)
      logMessage?.let { loggedMessages.add(it) }
      i = nextIndex
    }

    return Triple(duration, phaseTimings, loggedMessages)
  }

  /** Parse a single report section from markdown content */
  private fun parseReportSection(lines: List<String>, startIndex: Int): Pair<ReportSection?, Int> {
    return when {
      startIndex >= lines.size -> null to startIndex + 1
      else -> {
        val headerLine = lines[startIndex]
        val timestamp = headerLine.extractTimestamp()

        timestamp?.let {
          val endIndex = lines.findSectionEndIndex(startIndex)
          val (_, _, loggedMessages) = extractSectionContent(lines, startIndex, endIndex, timestamp)

          val reportSection = ReportSection(timestamp = timestamp, loggedMessages = loggedMessages)

          reportSection to endIndex
        } ?: (null to startIndex + 1)
      }
    }
  }

  /** Generate complete report with TOC and all sections */
  private fun generateCompleteReport(sections: List<ReportSection>): String {
    return buildString {
      appendLine("# ${CoreConstants.PRODUCT_NAME} Processor Reports")
      appendLine()

      // Generate TOC if multiple sections
      if (sections.size > 1) {
        appendLine("## 📋 Table of Contents")
        sections.forEachIndexed { index, section ->
          val reportNumber = index + 1
          val timestamp =
            section.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
          val anchor = generateAnchor(reportNumber, section.timestamp)
          appendLine("- [Report $reportNumber - $timestamp](#$anchor)")
        }
        appendLine()
        appendLine("---")
        appendLine()
      }

      // Generate all report sections
      sections.forEachIndexed { index, section ->
        val reportNumber = index + 1
        appendReportSection(this, reportNumber, section)
        if (index < sections.size - 1) {
          appendLine()
          appendLine("---")
          appendLine()
        }
      }
    }
  }

  /** Generate a single report section */
  private fun appendReportSection(
    builder: StringBuilder,
    reportNumber: Int,
    section: ReportSection,
  ) {
    val timestamp = section.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    val anchor = generateAnchor(reportNumber, section.timestamp)

    builder.apply {
      appendLine("## $anchor")
      appendLine("**Generated:** $timestamp")
      appendLine()

      appendLine("### 📋 Detailed Log")
      appendLine("```")
      section.loggedMessages.forEach { logMsg -> appendLine(formatLogMessage(logMsg)) }
      appendLine("```")
    }
  }

  /** Generate anchor for markdown navigation */
  private fun generateAnchor(reportNumber: Int, timestamp: LocalDateTime): String {
    val timestampStr = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss"))
    return "report-$reportNumber---$timestampStr"
  }
}

/** Global reporter instance - initialized by MetalasticSymbolProcessorProvider */
val reporter: Reporter = ReporterFactory.currentReporter()

/** Factory for creating Reporter instances. */
object ReporterFactory {
  private var currentReporter: Reporter? = null

  fun currentReporter() = currentReporter ?: createDisabled()

  /**
   * Create a Reporter from processor options. By default, reporting is disabled for performance.
   */
  fun initialize(options: ProcessorOptions, kspLogger: KSPLogger): Reporter {
    val reporter =
      if (options.isReportingEnabled) {
        DefaultReporter(options, kspLogger)
      } else {
        createDisabled()
      }
    currentReporter = reporter
    return reporter
  }

  /** Create a disabled reporter that performs no operations. */
  fun createDisabled(): Reporter =
    object : Reporter {
        override fun debug(message: () -> String) = Unit

        override fun exception(exception: Throwable, message: () -> String) = Unit

        override fun generateReport(): String? = null

        override fun writeReport(): Path? = null
      }
      .also { currentReporter = it }
}
