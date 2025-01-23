rootProject.name = "thegreensuits"

include("shared:api")

// - Commons subprojects
include("core")
include("proxy")

// - Servers subprojects
include("servers:survival")
// include("servers:hub")