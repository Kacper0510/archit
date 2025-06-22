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

### Jako mod do Minecrafta, najnowsza wersja języka
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

# Zastosowanie języka
### archit to własny język skryptowy zaprojektowany do:
- generowania budowli i struktur w świecie Minecrafta
- obserwowania efektów w czasie rzeczywistym podczas rozgrywki
- eksportu wyników do zewnętrznych modeli 3D poza grą

# Cechy języka
### 1. Interpreter
- Kod jest interpretowany „w locie” – nie trzeba osobno kompilować.

### 2. Silne typowanie
- Każda zmienna ma określony typ (number, real, logic, string, material, list, map, enum).
- Nie można mieszać typów bez wyraźnego rzutowania.

### 3. Brak null
- Nie ma wartości null; zamiast tego stosujemy puste listy, puste mapy lub odpowiednie typy.

### 4. Brak automatycznej konwersji typów
- Jeśli potrzebujesz zmienić typ, użyj funkcji rzutowania, które są zaimplementowane w standardowej bibliotece naszego języka

### 5. Przeciążenia funkcji
- Funkcje o tej samej nazwie mogą mieć różne zestawy parametrów.

### 6. Przekazywanie przez kopiowanie
- Argumenty do funkcji są kopiowane. Oryginalne dane nie są modyfikowane przez funkcję.

# Typy danych

## number
typ przeznaczony do przechowywania całkowitych wartości liczbowych. Wartości można zapisywać z użyciem podkreśleń („_”) w celu zwiększenia czytelności dużych liczb (np. 20_000).

### Przykłady:
```bash
var x: number = 10
var y: number = 20_000
```

## real
typ do liczb rzeczywistych, czyli zawierających część dziesiętną. Obsługuje zarówno zwykłe zapisy dziesiętne (3.14), jak i notację naukową (-1e10). Używaj go wszędzie tam, gdzie potrzebna jest precyzja ułamkowa.

### Przykłady:
```bash
var pi: real = 3.14
var big: real = -1e10
```

## logic
typ logiczny przechowujący jedną z dwóch wartości: true (prawda) lub false (fałsz). Przydaje się w instrukcjach warunkowych i pętlach dla kontroli przepływu programu.

### Przykłady:
```bash
var flag1: logic = true
var flag2: logic = false
```

## string
typ tekstowy, używany do przechowywania ciągów znaków. Ciąg musi być ujęty w pojedyncze apostrofy. Wspiera podstawową interpolację – wstawianie wartości zmiennych do tekstu za pomocą składni "{zmienna}".

### Przykłady:
``` bash
var name: string = 'Jan'
var full: string = "{name} Kowalski" // po interpolacji: "Jan Kowalski"
```

## material
typ specjalny służący do identyfikacji bloków w świecie Minecraft. Wartość to zawsze para minecraft:id (np. minecraft:stone, minecraft:dirt), ale dozwolone jest też użycie skróconej formy :dirt, która przyjmuje domyślną przestrzeń nazw. Dzięki temu zmienne typu material można bezpośrednio przekazywać do funkcji takich jak np. place.

### Przykłady:
```bash
var mat1: material = minecraft:stone
var mat2: material = :dirt
```

## list
typ tablicowy przechowujący uporządkowaną sekwencję elementów jednego typu (np. [number], [string], [material] itp.). Listę można zainicjować zestawem konkretnych wartości, np. [1, 2, 3] czy ['a', 'b'].

### Pustą listę można utworzyć wyłącznie przy deklaracji zmiennej z dokładnie określonym typem:
```bash
var nums: [number] = []
var matrix: [[number]] = [[], [3]]
```

W innych kontekstach (np. wywołania funkcji) literał [] jest niedozwolony, ponieważ prowadzi do niejednoznaczności przeciążeń.

### Przykłady prawidłowego użycia:
```bash
var nums: [number] = [1, 2, 3]
var words: [string] = ['a', 'b']
var emptyMatrix: [[number]] = [[], [3]]
```

### Przykład nieprawidłowego użycia:
```bash
print([])  // nie wiadomo, którą przeciążoną funkcję print wybrać
```

## map
słownikowa struktura klucz→wartość. Kluczami i wartościami mogą być dowolne obsługiwane typy (np. number, string, material). Mapa może być pusta lub zainicjowana dowolną liczbą par. Pozwala szybko wyszukiwać dane po kluczu.

### Przykłady:
```bash
var m1: |number -> string| = |1 -> 'a', 2 -> 'b'|
var m2: |number -> material| = || // pusta mapa
```

## enum
wyliczenie zestawu nazwanych stałych. Każdy element enum jest identyfikatorem poprzedzonym znakiem $. Służy do definiowania ograniczonego zbioru wartości, np. kierunków czy stanów, co ułatwia kontrolę poprawności kodu.

### Przykłady:
```bash
var dir: <up, down, left, right> = $up
move $posx
```

# Standardowa biblioteka

## 1. sin
Zwraca sinus kąta (w radianach).

### Przykład:
```bash
sin(value: real): real
```

## 2. cos
Zwraca cosinus kąta (w radianach).

### Przykład:
```bash 
cos(value: real): real
```

## 3. tan
Zwraca tangens kąta (w radianach).

### Przykład:
```bash
tan(value: real): real
```

## 4. log
Oblicza logarytm wartości o zadanej podstawie.

### Przykłady:
```bash
log(base: real, value: real): real
log(base: number, value: number): real
```

## 5. sign
Zwraca znak liczby: –1, 0 lub 1.

### Przykłady:
```bash
sign(value: real): number
sign(value: number): number
```

## 6. toRadians
Konwertuje wartość w stopniach na radiany.

### Przykład:
```bash 
toRadians(deg: real): real
```

## 7. toDegrees
Konwertuje wartość w radianach na stopnie.

### Przykład:
```bash
toDegrees(rad: real): real
```

## 8. args
Zwraca ciąg wszystkich argumentów przekazanych do skryptu.

### Przykład:
```bash
args(): string
```

## 9. random
Generuje losową liczbę z przedziału (min, max).

### Przykłady:
```bash
random(min: real, max: real): real
random(min: number, max: number): number
```

## 10. seed
Ustawia ziarno generatora liczb losowych.

### Przykłady:
```bash
seed(value: number)
seed(value: real)
```

## 11. length
Zwraca długość (liczbę znaków) podanego tekstu.

### Przykład:
```bash
length(text: string): number
```

## 12. upper
Zwraca tekst w wersji z dużymi literami.

### Przykład:
```bash
upper(text: string): string
```

## 13. lower
Zwraca tekst w wersji z małymi literami.

### Przykład:
```bash
lower(text: string): string
```

## 14. contains
Sprawdza, czy tekst zawiera podciąg.

### Przykład:
```bash
contains(text: string, part: string): boolean
```

## 15. startsWith
Sprawdza, czy tekst zaczyna się od podanego prefiksu.

### Przykład:
```bash
startsWith(text: string, prefix: string): boolean
```

## 16. endsWith
Sprawdza, czy tekst kończy się na podany sufiks.

### Przykład:
```bash
endsWith(text: string, suffix: string): boolean
```

## 17. indexOf
Zwraca indeks pierwszego wystąpienia podciągu lub –1, jeśli nie istnieje.

### Przykład:
```bash
indexOf(text: string, part: string): number
```

## 18. substring
Wydobywa fragment tekstu od indeksu begin do end (nie włącznie).

### Przykład:
```bash
substring(text: string, begin: number, end: number): string
```

## 19. replace
Zastępuje wszystkie wystąpienia target w tekście przez replacement.

### Przykład:
```bash
replace(text: string, target: string, replacement: string): string
```

## 20. trim
Usuwa białe znaki z początku i końca tekstu.

### Przykład:
```bash
trim(text: string): string
```

## 21. isEmpty
Zwraca true, jeśli tekst jest pusty.

### Przykład:
```bash
isEmpty(text: string): boolean
```

## 22. equals
Porównuje dwa ciągi, uwzględniając wielkość liter.

### Przykład:
```bash
equals(a: string, b: string): boolean
```

## 23. equalsIgnoreCase
Porównuje dwa ciągi, ignorując wielkość liter.

### Przykład:
```bash
equalsIgnoreCase(a: string, b: string): boolean
```

## 24. compareTo
Porównuje leksykograficznie dwa ciągi; zwraca ujemną liczbę, 0 lub dodatnią.

### Przykład:
```bash
compareTo(a: string, b: string): number
```

## 25. matches
Sprawdza, czy tekst pasuje do wyrażenia regularnego.

### Przykład:
```bash
matches(text: string, pattern: string): boolean
```

## 26. print
Wypisuje komunikat w konsoli skryptu.

### Przykład:
```bash
print(message: string)
```

## 27. move
Przesuwa kursor w świecie Minecraft.

### Przykłady:
```bash
move(direction: <posx, negx, posy, negy, posz, negz>)
move(x: number, y: number, z: number)
move(vector: [number])
```

## 28. position
Zwraca bieżące współrzędne kursora jako listę [x, y, z].

### Przykład:
```bash
position(): [number]
```

## 29. as_real
Rzutuje wartość number lub tekst na real.

### Przykłady:
```bash
as_real(value: number): real
as_real(value: string): real
```

## 30. as_number
Rzutuje wartość real lub tekst na number.

### Przykłady:
```bash
as_number(value: real): number
as_number(value: string): number
```

## 31. as_material
Rzutuje tekst w formacie namespace:id (lub :id) na material.

### Przykład:
```bash
as_material(value: string): material
```

## 32. sqrt
Oblicza pierwiastek kwadratowy.

### Przykłady:
```bash
sqrt(value: real): real
sqrt(value: number): real
```

## 33. abs
Zwraca wartość bezwzględną.

### Przykłady:
```bash
abs(value: real): real
abs(value: number): number
```

## 34. floor
Zaokrągla w dół do najbliższej liczby całkowitej.

### Przykład:
```bash
floor(value: real): number
```

## 35. ceil
Zaokrągla w górę do najbliższej liczby całkowitej.

### Przykład:
```bash
ceil(value: real): number
```

## 36. round
Zaokrągla do najbliższej liczby całkowitej.

### Przykład:
 ```bash
 round(value: real): number
 ```

# Składnia języka

## Operatory arytmetyczne
Służą do podstawowych operacji na liczbach (zarówno number, jak i real).

- \+ -  dodawanie

- \- - odejmowanie

- \* – mnożenie

- / – dzielenie

- ^ – potęgowanie

- % – modulo (reszta z dzielenia)

### Każdy z tych operatorów ma również wersję przypisania:
```
+=, -=, *=, /=, ^=, %=
```

## Operatory logiczne
Pozwalają łączyć i negować warunki logiczne (logic).

- and – koniunkcja (i)

- or – alternatywa (lub)

- not – negacja (nie)

## Operatory porównań
Używane w wyrażeniach warunkowych do porównywania wartości:

- == – równe

- != – różne

- \> – większe

- \>= – większe lub równe

- < – mniejsze

- <= – mniejsze lub równe

## Komentarze
Pozwalają dodawać wyjaśnienia w kodzie, które są ignorowane przez interpreter.

### Jednoliniowy:
wszystko po // aż do końca linii

### Wieloliniowy:
tekst między /* i */

## Instrukcja warunkowa
Pozwala wykonywać różne fragmenty kodu w zależności od wartości wyrażenia logicznego.

### Przykład:
```
if <warunek1> {  
    // wykona się, gdy warunek1 jest prawdziwy  
} else if <warunek2> {  
    // jeżeli warunek1 był fałszywy, a warunek2 prawdziwy  
} else {  
    // gdy żaden z powyższych warunków nie był prawdziwy  
}  
```

## Pętle
### while
Powtarza blok kodu tak długo, jak długo warunek pozostaje prawdziwy.

```
while <warunek> {  
    // kod wykonywany w każdej iteracji  
} 
```

### repeat
Powtarza blok kodu określoną liczbę razy.

```
repeat <N> {  
    // kod wykonywany N razy  
} 
```

### break i continue
break – natychmiastowo przerywa wykonywanie pętli

continue – przerywa bieżącą iterację pętli i przechodzi do następnej

# Przykładowy skrypt
```
function factorial_it(x: number): number {
    var i: number = 1;
    while x > 0 {
        i *= x;
        x -= 1;
    }
    return i;
}

function factorial_rec(x: number): number {
    if x == 0 or x == 1 {
        return 1;
    }
    return x * factorial_rec(x-1);
}

print "{factorial_it(5)} {factorial_rec(5)}";
```
