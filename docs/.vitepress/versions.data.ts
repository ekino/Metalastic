export default {
  load() {
    return {
      // Core versions
      metalastic: '1.1.0',
      ksp: '2.3.4',
      kotlin: '2.3.0',

      // DSL versions
      dsl: {
        rolling: '1.1.0',      // Base artifact (6.0.x currently)
        frozen55: '1.1.0',     // 5.4-5.5 frozen
        frozen53: '1.1.0'      // 5.0-5.3 frozen
      },

      // Spring Data ES versions (brought transitively by DSL artifacts)
      springDataES: {
        v60: '6.0.1',
        v55: '5.5.6',
        v53: '5.3.13'
      }
    }
  }
}
