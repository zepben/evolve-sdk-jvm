/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

const zepbenDocusaurusPreset = require("@zepben/docusaurus-preset");
const versions = require("./versions.json");

module.exports = {
    title: "Evolve SDK (JVM)",
    tagline: "",
    url: "https://zepben.github.io/evolve/docs/jvm-sdk/",
    baseUrl: "/evolve/docs/jvm-sdk/",
    onBrokenLinks: "warn",
    favicon: "img/favicon.ico",
    organizationName: "zepben",
    projectName: "evolve-sdk-jvm",
    themeConfig: {
        ...zepbenDocusaurusPreset.defaultThemeConfig,
        colorMode: {
            defaultMode: "light",
            disableSwitch: false,
            respectPrefersColorScheme: true,
        },
        navbar: {
            logo: {
                alt: "Zepben",
                src: "img/logo.svg",
                srcDark: "img/logo-dark.svg",
                href: "https://www.zepben.com/",
            },
            items: [
                {
                    to: "https://zepben.github.io/evolve/docs",
                    label: "Evolve",
                    position: "left",
                },
                {
                    to: "/",
                    activeBasePath: "docs",
                    label: "Docs",
                    position: "left",
                },
                {
                    to: "release-notes",
                    activeBasePath: "release-notes",
                    label: "Release Notes",
                    position: "right",
                },
                {
                    type: "docsVersionDropdown",
                    position: "right",
                },
                {
                    href: "https://github.com/zepben/evolve-sdk-jvm/",
                    position: 'right',
                    className: 'header-github-link',
                    'aria-label': 'GitHub repository',
                },
            ],
        },
        footer: {
            style: "dark",
            links: [],
            copyright: `Copyright © ${new Date().getFullYear()} Zeppelin Bend Pty. Ltd.`,
        },
        prism: {
            additionalLanguages: ['kotlin', 'json'],
        },
        algolia: {
            ...zepbenDocusaurusPreset.defaultThemeConfig.algolia,
            searchParameters: {
                facetFilters: ["project:jvm-sdk"]
            }
        },
        metadatas: [{name: 'docsearch:project', content: 'jvm-sdk'}],
    },
    presets: [
        [
            "@zepben/docusaurus-preset",
            {
                docs: {
                    routeBasePath: '/',
                    sidebarPath: require.resolve("./sidebars.js"),
                    versions: versions.reduce((acc, curr) => {
                        acc[curr] = {label: curr, path: curr};
                        return acc;
                    }, {})
                },
                theme: {
                    customCss: require.resolve("./src/css/custom.css"),
                },
            },
        ],
    ],
};
