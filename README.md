# automatic-goggles
Taller 1 de backend avanzado

## Testing

- Bash/Git Bash (all tests): `./mvnw test`
- Bash/Git Bash (behavior-oriented suite): `./mvnw -q -Dtest='*MapperTest,*ServiceTest,*RepositoryTest,*ControllerTest,SecurityConfigIntegrationTest' test`
- PowerShell/CMD (all tests): `mvnw.cmd test`
- PowerShell/CMD (behavior-oriented suite): `mvnw.cmd -q "-Dtest=*MapperTest,*ServiceTest,*RepositoryTest,*ControllerTest,SecurityConfigIntegrationTest" test`
