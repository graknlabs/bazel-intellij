load("//intellij_platform_sdk:build_defs.bzl", "select_for_plugin_api")

package(default_visibility = ["//visibility:public"])

licenses(["notice"])

java_library(
    name = "rust_internal",
    visibility = ["//visibility:private"],
    exports = select_for_plugin_api({
        "clion-2021.2": ["@rust_2021_2//:rust"],
        "clion-2021.3": ["@rust_2021_3//:rust"],
        "clion-2022.1": ["@rust_2022_1//:rust"],
        "clion-2022.2": ["@rust_2022_2//:rust"],
        "intellij-2021.2": ["@rust_2021_2//:rust"],
        "intellij-ue-2021.2": ["@rust_2021_2//:rust"],
        "intellij-2021.3": ["@rust_2021_3//:rust"],
        "intellij-ue-2021.3": ["@rust_2021_3//:rust"],
        "intellij-2022.1": ["@rust_2022_1//:rust"],
        "intellij-ue-2022.1": ["@rust_2022_1//:rust"],
        "intellij-2022.2": ["@rust_2022_2//:rust"],
        "intellij-ue-2022.2": ["@rust_2022_2//:rust"],
        "default": [],
    }),
)

java_library(
    name = "rust_for_tests",
    testonly = 1,
    exports = [":rust_internal"],
)

java_library(
    name = "rust",
    neverlink = 1,
    exports = [":rust_internal"],
)
