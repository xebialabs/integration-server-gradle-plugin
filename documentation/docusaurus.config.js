const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

module.exports = {
    title: 'Integration Server',
    tagline: 'Integration Server Gradle Plugin for Deploy and Release Digital.ai',
    url: 'https://xebialabs.github.io/',
    baseUrl: '/integration-server-gradle-plugin/',
    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',
    favicon: 'img/digital_ai_deploy.svg',
    organizationName: 'xebialabs',
    projectName: 'integration-server-gradle-plugin',
    themeConfig: {
        navbar: {
            title: 'Integration Server',
            logo: {
                alt: 'Integration Server Deploy Digital.ai',
                src: 'img/digital_ai_deploy.svg',
            },
            items: [
                {
                    type: 'doc',
                    docId: 'intro',
                    position: 'left',
                    label: 'Tutorial',
                },
                {
                    type: 'docsVersionDropdown',
                    position: 'right',
                },
                {
                    href: 'https://github.com/xebialabs/integration-server-gradle-plugin',
                    label: 'GitHub',
                    position: 'right',
                },
                {
                    to: 'blog',
                    label: 'Blog',
                    position: 'left'
                }
            ],
        },
        footer: {
            style: 'dark',
            links: [
                {
                    title: 'Docs',
                    items: [
                        {
                            label: 'Tutorial',
                            to: '/docs/intro',
                        },
                        {
                            label: 'Blog',
                            to: '/blog',
                        },
                        {
                            label: 'GitHub',
                            href: 'https://github.com/xebialabs/integration-server-gradle-plugin',
                        },
                    ],
                },
            ],
            copyright: `Copyright © ${new Date().getFullYear()} Integration Server Digital.ai`,
        },
        prism: {
            theme: lightCodeTheme,
            darkTheme: darkCodeTheme,
        },
    },
    presets: [
        [
            '@docusaurus/preset-classic',
            {
                docs: {
                    sidebarPath: 'sidebars.js',
                    lastVersion: "current",
                    versions: {
                        current: {
                            label: '10.4.0',
                            path: ''
                        }
                    },
                },
                blog: {
                    showReadingTime: true
                },
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            },
        ],
    ],
};
