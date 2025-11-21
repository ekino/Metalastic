import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'Metalastic',
  description: 'Type-safe metamodel generator for Elasticsearch in Kotlin',
  base: '/Metalastic/',  // GitHub Pages base URL

  head: [
    ['link', { rel: 'icon', type: 'image/svg+xml', href: '/Metalastic/favicon.svg' }],
    ['link', { rel: 'icon', type: 'image/png', sizes: '96x96', href: '/Metalastic/favicon-96x96.png' }],
    ['link', { rel: 'apple-touch-icon', sizes: '180x180', href: '/Metalastic/apple-touch-icon.png' }],
    ['link', { rel: 'manifest', href: '/Metalastic/site.webmanifest' }]
  ],

  themeConfig: {
    logo: '/logo.png',

    nav: [
      { text: 'Home', link: '/' },
      { text: 'Guide', link: '/guide/getting-started' },
      { text: 'GitHub', link: 'https://github.com/ekino/Metalastic' }
    ],

    sidebar: [
      {
        text: 'Getting Started',
        items: [
          { text: 'Quick Start', link: '/guide/getting-started' },
          { text: 'Configuration', link: '/guide/configuration' },
          { text: 'Understanding Metamodels', link: '/guide/understanding-metamodels' }
        ]
      },
      {
        text: 'Reference',
        collapsed: false,
        items: [
          { text: 'Field Types', link: '/guide/field-types-reference' },
          { text: 'Query DSL', link: '/guide/query-dsl-guide' }
        ]
      },
      {
        text: 'Examples',
        items: [
          { text: 'Complete Examples', link: '/guide/examples' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/ekino/Metalastic' }
    ],

    footer: {
      message: 'Released under the MIT License.',
      copyright: 'Copyright © 2024-present ekino'
    },

    search: {
      provider: 'local'
    }
  }
})
