include("dummy")

rootProject.name = "lines"

buildCache {
    local {
        isEnabled = !System.getenv().containsKey("CI")
    }
}

