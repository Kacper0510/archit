# archit
### Autorzy: Emil Wajda, Dawid Węcirz, Kacper Wojciuch

---
---

<br><br>

# Instrukcja obsługi

## Jak uruchomić  
`archit` obsługuje dwa tryby działania:  
1. Jako mod do Minecrafta, który pozwala uruchamiać skrypty w grze i oglądać wyniki w czasie rzeczywistym.  
2. Jako samodzielny program, który pozwala uruchamiać skrypty z terminala i eksportować wyniki do obiektu 3D.  

### Jako mod do Minecrafta, najnowsza wersja  
- Utwórz instancję Minecrafta z wersją ustawioną na **1.21.5**  
- Zainstaluj [Fabric](https://fabricmc.net/) jako modloader  
- Umieść [Fabric API](https://modrinth.com/mod/fabric-api/versions) w folderze `mods`  
- Pobierz najnowszą wersję moda z [GitHub Actions](https://github.com/Kacper0510/archit/actions?query=branch%3Amaster) i również umieść ją w folderze `mods`  

### Jako mod do Minecrafta, lokalnie  
- Sklonuj repozytorium:  
```bash
$ git clone https://github.com/Kacper0510/archit
$ cd archit
```  
- Uruchom zadanie Gradle, aby otworzyć klienta lub serwer Minecrafta:  
```bash
$ ./gradlew runClient # LUB ./gradlew runServer
```  

### Jako samodzielny program, najnowsza wersja  
- Pobierz najnowszy plik JAR z [GitHub Actions](https://github.com/Kacper0510/archit/actions?query=branch%3Amaster)  
- Uruchom za pomocą Javy:  
```bash
$ java -jar archit.jar <ścieżka do pliku wejściowego>
```  

### Jako samodzielny program, lokalnie  
- Sklonuj repozytorium:  
```bash
$ git clone https://github.com/Kacper0510/archit
$ cd archit
```  
- Uruchom za pomocą Gradle:  
```bash
$ ./gradlew run --args="<ścieżka do pliku wejściowego>"
```  



