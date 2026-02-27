export default {
  load() {
    return {
      // Core versions
      metalastic: '1.2.2',
      ksp: '2.3.6',
      kotlin: '2.3.10',

      // DSL versions
      dsl: {
        rolling: '1.2.2',      // Base artifact (6.0.x currently)
        frozen55: '1.2.2',     // 5.4-5.5 frozen
        frozen53: '1.2.2'      // 5.0-5.3 frozen
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
