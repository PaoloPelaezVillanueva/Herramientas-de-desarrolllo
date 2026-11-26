param(
    [string]$RepoPath = (Join-Path $PSScriptRoot '..\..')
)

$repo = (Resolve-Path -LiteralPath $RepoPath).Path
$fixture = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot 'e2e-data.sql')).Path.Replace('\', '/')

$env:SPRING_DATASOURCE_URL = 'jdbc:h2:mem:tambo_e2e;MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1'
$env:SPRING_DATASOURCE_DRIVER_CLASS_NAME = 'org.h2.Driver'
$env:SPRING_DATASOURCE_USERNAME = 'sa'
$env:SPRING_DATASOURCE_PASSWORD = ''
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = 'create-drop'
$env:SPRING_JPA_DEFER_DATASOURCE_INITIALIZATION = 'true'
$env:SPRING_SQL_INIT_MODE = 'always'
$env:SPRING_SQL_INIT_DATA_LOCATIONS = "file:$fixture"
$env:JWT_SECRET = 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA='
$env:SERVER_PORT = '18080'
$env:SERVER_ADDRESS = '127.0.0.1'
$env:LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY = 'WARN'
$env:SPRING_JPA_SHOW_SQL = 'false'

Push-Location $repo
try {
    .\mvnw.cmd -q spring-boot:run '-Dspring-boot.run.useTestClasspath=true'
} finally {
    Pop-Location
}
