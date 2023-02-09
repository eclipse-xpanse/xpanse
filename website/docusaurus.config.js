// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Open Services Cloud',
  tagline: 'Open Services Cloud is an Open Source project allowing to easily implement native managed service on any cloud provider.',
  url: 'https://huaweicloud.github.io/',
  baseUrl: '/xpanse/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'huaweicloud', // Usually your GitHub org/user name.
  projectName: 'xpanse', // Usually your repo name.

  // Even if you don't use internalization, you can use this field to set useful
  // metadata like html lang. For example, if your site is Chinese, you may want
  // to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
        },
        blog: {
          showReadingTime: true,
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: 'Open Services Cloud',
        logo: {
          alt: 'Open Services Cloud',
          src: 'img/logo.png',
        },
        items: [
          {
            type: 'doc',
            docId: 'intro',
            position: 'right',
            label: 'Documentation',
          },
          {to: '/download', label: 'Download', position: 'right'},
          {to: '/blog', label: 'Blog', position: 'right'},
          {to: '/demo', label: 'Demo', position: 'right'},
          {
            href: 'https://github.com/huaweicloud/osc',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Docs',
            items: [
              {
                label: 'Getting Started',
                to: '/docs/intro',
              },
							{
								label: 'Architecture',
								to: '/docs/architecture',
							},
              {
                label: 'Configuration Language',
                to: '/docs/ocl',
              },
              {
                label: 'Runtime',
                to: '/docs/runtime',
              },
              {
                label: 'API',
                to: '/docs/api',
              },
							{
								label: 'Supported Cloud',
								to: '/docs/supported-cloud',
							},
							{
								label: 'Resources',
								to: '/docs/resources',
							},
							{
								label: 'Plugin',
								to: '/docs/plugin',
							},
            ],
          },
          {
            title: 'Community',
            items: [
							{
								label: 'Contribute',
								to: '/docs/contribute',
							},
              {
                label: 'Mailing Lists',
                href: 'https://accounts.eclipse.org/mailing-list/osc-wg',
              },
              {
                label: 'Slack',
                href: 'https://app.slack.com/client/T02U1MCB4HW/C02U1MCDB9N?cdn_fallback=2',
              },
              {
                label: 'Twitter',
                href: 'https://twitter.com/openservicescloud',
              },
            ],
          },
          {
            title: 'Project',
            items: [
              {
                label: 'Eclipse',
                href: 'https://www.eclipse.org',
              },
              {
                label: 'GitHub',
                href: 'https://github.com/huaweicloud/osc',
              },
              {
                label: 'Blog',
                href: '/blog',
              },
              {
                label: 'Demo',
                to: '/demo',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Eclipse Foundation.`,
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
      },
    }),
};

module.exports = config;
