package(default_visibility = ["//visibility:public"])

java_binary(
    name = "javad",
    main_class = "net.ajmichael.javad.App",
    runtime_deps = [":javadlib"],
)

java_library(
    name = "javadlib",
    srcs = glob(["*.java"]),
    deps = [
        "//java/net/ajmichael/classfile",
        "//java/net/ajmichael/converters",
        "//java/net/ajmichael/javafile",
        "//third_party:auto_value",
    ],
)
