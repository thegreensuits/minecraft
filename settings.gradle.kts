rootProject.name = "thegreensuits"

include("plugins:api")

// - Commons subprojects
include("plugins:core")
include("plugins:proxy")

// - Servers subprojects
include("plugins:servers:survival")
// include("servers:hub")