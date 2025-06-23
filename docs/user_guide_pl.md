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

### Aby zarządzać skryptami z poziomu gry Minecraft

Należy stworzyć folder `archit-scripts` w folderze `.minecraft` i następnie dodać tam swoje skrypty.

Komendy do testowania skryptów:

- `archit run <skrypt> [argumenty]`

    Uruchamia podany skrypt. Plik skryptu może przyjmować opcjonalne argumenty,
    które przekażesz po nazwie pliku.

    Przykład:

    ```
    archit run budowla.arch x y z
    ```

- `archit stop <id_uruchomienia>`

    Zatrzymuje wykonywanie konkretnego procesu skryptu, 
    identyfikowanego numerem przydzielonym przy uruchomieniu. 
    Użyj tej komendy, gdy chcesz przerwać długotrwałe lub niepotrzebne 
    uruchomienie.

    Przykład:

    ```
    archit stop test.archit@12:23:32
    ```

    (zaimplementowane są również podpowiedzi w grze w celu łatwiejszego użycia tego skryptu, więc godzina jak i nazwa skryptu będzie wyświetlana użytkownikowi)

- `archit animate <czas_na_wywołanie_funkcji> <skrypt> [argumenty]`

    Wyświetla animację działania skryptu w czasie rzeczywistym,
    pauzując się na określony czas (w tickach gry) 
    po każdym wywołaniu funkcji z biblioteki (np. place, move). 
    Argumenty skryptu są opcjonalne. Dzięki animacji możesz zobaczyć 
    krok po kroku powstawanie struktury.

    Przykład:

    ```
    archit animate 5 generator.archit 10 20 30
    ```

    W powyższym przykładzie każdy krok wywołania funkcji będzie opóźniony o $5 \cdot 50 = 250 \, \text{ms}$, a do skryptu zostaną przekazane trzy argumenty.

Dzięki tym trzem komendom możesz w prosty sposób uruchamiać, zatrzymywać i wizualizować swoje skrypty `archit`.

## Zastosowanie języka

### archit to własny język skryptowy zaprojektowany do:

- generowania budowli i struktur w świecie Minecrafta

- obserwowania efektów w czasie rzeczywistym podczas rozgrywki

- eksportu wyników do zewnętrznych modeli 3D poza grą

## Cechy języka

### 1. Interpreter

- Kod jest interpretowany "w locie" – nie trzeba osobno kompilować.

### 2. Silne typowanie

- Każda zmienna ma określony typ (number, real, logic, string, material, list, map, enum).

- Nie można mieszać typów bez wyraźnego rzutowania.

### 3. Brak null

- Nie ma wartości `null`; zamiast tego stosujemy puste listy, puste mapy lub odpowiednie typy.

### 4. Brak automatycznej konwersji typów

- Jeśli potrzebujesz zmienić typ, użyj funkcji rzutowania, które są zaimplementowane w standardowej bibliotece naszego języka.

### 5. Przeciążenia funkcji

- Funkcje o tej samej nazwie mogą mieć różne zestawy parametrów.

### 6. Przekazywanie przez kopiowanie
- Argumenty do funkcji są kopiowane. Oryginalne dane nie są modyfikowane przez funkcję.

## Typy danych

### number

Typ przeznaczony do przechowywania całkowitych wartości liczbowych. Wartości można zapisywać z użyciem podkreśleń ("_") w celu zwiększenia czytelności dużych liczb (np. 20_000).

Przykłady:

```
var x: number = 10;
var y: number = 20_000;
```

### real

Typ do liczb rzeczywistych, czyli zawierających część dziesiętną. Obsługuje zarówno zwykłe zapisy dziesiętne (3.14), jak i notację naukową (-1e10). Używaj go wszędzie tam, gdzie potrzebna jest precyzja ułamkowa.

Przykłady:

```
var pi: real = 3.14;
var big: real = -1e10;
```

### logic

Typ logiczny przechowujący jedną z dwóch wartości: true (prawda) lub false (fałsz). Przydaje się w instrukcjach warunkowych i pętlach dla kontroli przepływu programu.

Przykłady:

```
var flag1: logic = true;
var flag2: logic = false;
```

### string

Typ tekstowy, używany do przechowywania ciągów znaków. Ciąg musi być ujęty w pojedyncze apostrofy. Wspiera interpolację – wstawianie wartości wyrażeń do tekstu za pomocą składni "{wyrażenie}".

Przykłady:

```
var name: string = 'Jan';
var full: string = "{name} Kowalski"; // po interpolacji: "Jan Kowalski"
```

### material

Typ specjalny służący do identyfikacji bloków w świecie Minecraft. Wartość to zawsze para `namespace:id` (np. minecraft:stone, minecraft:dirt), ale dozwolone jest też użycie skróconej formy :dirt, która przyjmuje domyślną przestrzeń nazw (minecraft). Dzięki temu zmienne typu material można bezpośrednio przekazywać do funkcji takich jak np. place.

Przykłady:

```
var mat1: material = minecraft:stone;
var mat2: material = :dirt;
```

### list

Typ tablicowy przechowujący uporządkowaną sekwencję elementów jednego typu (np. \[number\], \[string\], \[material\] itp.). Listę można zainicjować zestawem konkretnych wartości, np. \[1, 2, 3\] czy \['a', 'b'\].

Pustą listę można utworzyć wyłącznie przy deklaracji zmiennej z dokładnie określonym typem:

```
var nums: [number] = [];
var matrix: [[number]] = [[], [3]];
```

W innych kontekstach (np. wywołania funkcji) literał [] jest niedozwolony, ponieważ prowadzi do niejednoznaczności przeciążeń.

Przykłady prawidłowego użycia:

```
var nums: [number] = [1, 2, 3];
var words: [string] = ['a', 'b'];
var emptyMatrix: [[number]] = [[], [3]];
```

Przykład nieprawidłowego użycia:

```
print [];  // nie wiadomo, którą przeciążoną funkcję print wybrać
```

### map

Słownikowa struktura `klucz -> wartość`. Kluczami i wartościami mogą być dowolne obsługiwane typy (np. number, string, material). Mapa może być pusta lub zainicjowana dowolną liczbą par. Pozwala szybko wyszukiwać dane po kluczu.

Przykłady:

```
var m1: |number -> string| = |1 -> 'a', 2 -> 'b'|;
var m2: |number -> material| = ||;  // pusta mapa
```

### enum

Wyliczenie zestawu nazwanych stałych. Każdy element enum jest identyfikatorem poprzedzonym znakiem $. Służy do definiowania ograniczonego zbioru wartości, np. kierunków czy stanów, co ułatwia kontrolę poprawności kodu.

Przykłady:

```
var dir: <up, down, left, right> = $up;
move $posx;
```

## Składnia języka

### Operatory arytmetyczne

Służą do podstawowych operacji na liczbach (zarówno number, jak i real).

- `+` -  dodawanie

- `-` - odejmowanie

- `*` – mnożenie

- `/` – dzielenie

- `^` – potęgowanie

- `%` – modulo (reszta z dzielenia)

Każdy z tych operatorów ma również wersję przypisania:

```
+=, -=, *=, /=, ^=, %=
```

### Operatory logiczne

Pozwalają łączyć i negować warunki logiczne (logic).

- `and` – koniunkcja (i)

- `or` – alternatywa (lub)

- `not` – negacja (nie)

### Operatory porównań

Używane w wyrażeniach warunkowych do porównywania wartości:

- `==` – równe

- `!=` – różne

- `>` – większe

- `>=` – większe lub równe

- `<` – mniejsze

- `<=` – mniejsze lub równe

### Zmienne

Służą do przechowywania danych. Deklaracja zmiennej wymaga podania typu, nazwy i wartości początkowej.

Przykład:

```
var x: number = 10;
var y: real = 3.14;
var flag: logic = true;
var name: string = 'Jan';
```

Po zadeklarowaniu zmiennej można ją modyfikować, przypisując nową wartość:

```
x = 20;  // zmiana wartości x
flag = false;  // zmiana wartości flag
name = 'Anna';  // zmiana wartości name
```

### Komentarze

Pozwalają dodawać wyjaśnienia w kodzie, które są ignorowane przez interpreter.

- **Jednoliniowy**: wszystko po // aż do końca linii

- **Wieloliniowy**: tekst między /* i */

### Instrukcja warunkowa

Pozwala wykonywać różne fragmenty kodu w zależności od wartości wyrażenia logicznego.

Przykład:

```
if <warunek1> {  
    // wykona się, gdy warunek1 jest prawdziwy  
} else if <warunek2> {  
    // jeżeli warunek1 był fałszywy, a warunek2 prawdziwy  
} else {  
    // gdy żaden z powyższych warunków nie był prawdziwy  
}  
```

### Pętle

- **while**

    Powtarza blok kodu tak długo, jak długo warunek pozostaje prawdziwy.

    ```
    while <warunek> {  
        // kod wykonywany w każdej iteracji  
    } 
    ```

- **repeat**

    Powtarza blok kodu określoną liczbę razy.

    ```
    repeat <N> {  
        // kod wykonywany N razy  
    } 
    ```

### break i continue

- **break** – natychmiastowo przerywa wykonywanie pętli

- **continue** – przerywa bieżącą iterację pętli i przechodzi do następnej

### Wywoływanie funkcji

W języku `archit` funkcje są podstawowym sposobem organizacji kodu. Można je wywoływać przekazując odpowiednie argumenty oraz pobierając wartość wynikową.

Przykład:

```
var wynik: real = nazwa_funkcji(arg1, arg2);
```

W niektórych miejscach, gdzie da się wykluczyć niejednoznaczność przekazywania zagnieżdżonych parametrów, funkcje mogą być wywoływane bez użycia nawiasów:

```
nazwa_funkcji arg1, arg2;
```


### Definiowanie własnych funkcji

Definiowanie własnych funkcji pozwala na tworzenie kodu wielokrotnego użytku. Funkcje mogą przyjmować argumenty i zwracać wartość.

Przykład:

```
function nazwa_funkcji(parametr1: number, parametr2: real) -> real {
    return as_real(parametr1) + parametr2;
}
```

Fragment `-> real` oznacza, że funkcja zwraca wartość typu `real`. Jeśli funkcja nie zwraca wartości, można pominąć ten fragment oraz odpowiadające wywołania `return`.

Funkcje mogą używać rekurencji, czyli wywoływać same siebie.

### Zasięg zmiennych

Zasięg zmiennych określa, gdzie dana zmienna jest widoczna i dostępna w kodzie. Mimo to, można zawsze odwoływać się do zmiennych z zasięgu zewnętrznego, w tym do zmiennych globalnych i standardowej biblioteki. To zachowanie tyczy się również funkcji.

Każde użycie nawiasów klamrowych `{}` tworzy nowy zasięg. Można także podać same nawiasy klamrowe jako instrukcję.

Aby odwołać się do zmiennej z zasięgu zewnętrznego, wystarczy użyć jej nazwy. Jeśli zmienna o tej samej nazwie istnieje w bieżącym zasięgu, zostanie użyta ta lokalna wersja. Jeśli mimo to naprawdę chcesz odwołać się do zmiennej z zasięgu zewnętrznego, możesz użyć odpowiedniej liczby powtórzeń znaku `~`, wskazującego na ilość zmiennych do przeskoczenia w górę w hierarchii zasięgów.

## Standardowa biblioteka

01. `sin`

    Zwraca sinus kąta (w radianach).

    ### Przykład:

    ```
    sin(value: real): real
    ```

02. `cos`

    Zwraca cosinus kąta (w radianach).

    ### Przykład:

    ``` 
    cos(value: real): real
    ```

03. `tan`

    Zwraca tangens kąta (w radianach).

    ### Przykład:

    ```
    tan(value: real): real
    ```

04. `log`

    Oblicza logarytm wartości o zadanej podstawie.

    ### Przykłady:

    ```
    log(base: real, value: real): real
    log(base: number, value: number): real
    ```

05. `sign`

    Zwraca znak liczby: –1, 0 lub 1.

    ### Przykłady:

    ```
    sign(value: real): number
    sign(value: number): number
    ```

06. `to_radians`

    Konwertuje wartość w stopniach na radiany.

    ### Przykład:

    ``` 
    to_radians(deg: real): real
    ```

07. `to_degrees`

    Konwertuje wartość w radianach na stopnie.

    ### Przykład:

    ```
    to_degrees(rad: real): real
    ```

08. `args`

    Zwraca ciąg wszystkich argumentów przekazanych do skryptu.

    ### Przykład:

    ```
    args(): string
    ```

09. `random`

    Generuje losową liczbę z przedziału (min, max).

    ### Przykłady:

    ```
    random(min: real, max: real): real
    random(min: number, max: number): number
    ```

10. `seed`

    Ustawia ziarno generatora liczb losowych.

    ### Przykłady:

    ```
    seed(value: number)
    seed(value: real)
    ```

11. `length`

    Zwraca długość (liczbę znaków) podanego tekstu.

    ### Przykład:

    ```
    length(text: string): number
    ```

12. `upper`

    Zwraca tekst w wersji z dużymi literami.

    ### Przykład:

    ```
    upper(text: string): string
    ```

13. `lower`

    Zwraca tekst w wersji z małymi literami.

    ### Przykład:

    ```
    lower(text: string): string
    ```

14. `contains`

    Sprawdza, czy tekst zawiera podciąg.

    ### Przykład:

    ```
    contains(text: string, part: string): boolean
    ```

15. `starts_with`

    Sprawdza, czy tekst zaczyna się od podanego prefiksu.

    ### Przykład:

    ```
    startsWith(text: string, prefix: string): boolean
    ```

16. `ends_with`

    Sprawdza, czy tekst kończy się na podany sufiks.

    ### Przykład:

    ```
    endsWith(text: string, suffix: string): boolean
    ```

17. `index_of`

    Zwraca indeks pierwszego wystąpienia podciągu lub –1, jeśli nie istnieje.

    ### Przykład:

    ```
    index_of(text: string, part: string): number
    ```

18. `substring`

    Wydobywa fragment tekstu od indeksu begin do end (nie włącznie).

    ### Przykład:

    ```
    substring(text: string, begin: number, end: number): string
    ```

19. `replace`

    Zastępuje wszystkie wystąpienia target w tekście przez replacement.

    ### Przykład:

    ```
    replace(text: string, target: string, replacement: string): string
    ```

20. `trim`

    Usuwa białe znaki z początku i końca tekstu.

    ### Przykład:

    ```
    trim(text: string): string
    ```

21. `equals_ignore_case`

    Porównuje dwa ciągi, ignorując wielkość liter.

    ### Przykład:

    ```
    equals_ignore_case(a: string, b: string): boolean
    ```

22. `matches`

    Sprawdza, czy tekst pasuje do wyrażenia regularnego.

    ### Przykład:

    ```
    matches(text: string, pattern: string): boolean
    ```

23. `print`

    Wypisuje komunikat w konsoli skryptu.

    ### Przykład:

    ```
    print(message: ?)
    ```

24. `move`

    Przesuwa kursor w świecie Minecraft.

    ### Przykłady:

    ```
    move(direction: <posx, negx, posy, negy, posz, negz>)
    move(x: number, y: number, z: number)
    move(vector: [number])
    ```

25. `position`

    Zwraca bieżące współrzędne kursora jako listę [x, y, z].

    ### Przykład:

    ```
    position(): [number]
    ```

26. `as_real`

    Rzutuje wartość number lub tekst na real.

    ### Przykłady:

    ```
    as_real(value: number): real
    as_real(value: string): real
    ```

27. `as_number`

    Rzutuje wartość real lub tekst na number.

    ### Przykłady:

    ```
    as_number(value: real): number
    as_number(value: string): number
    ```

28. `as_material`

    Rzutuje tekst w formacie namespace:id (lub :id) na material.

    ### Przykład:

    ```
    as_material(value: string): material
    ```

29. `sqrt`

    Oblicza pierwiastek kwadratowy.

    ### Przykłady:

    ```
    sqrt(value: real): real
    sqrt(value: number): real
    ```

30. `abs`

    Zwraca wartość bezwzględną.

    ### Przykłady:

    ```
    abs(value: real): real
    abs(value: number): number
    ```

31. `floor`

    Zaokrągla w dół do najbliższej liczby całkowitej.

    ### Przykład:

    ```
    floor(value: real): number
    ```

32. `ceil`

    Zaokrągla w górę do najbliższej liczby całkowitej.

    ### Przykład:

    ```
    ceil(value: real): number
    ```

33. `round`

    Zaokrągla do najbliższej liczby całkowitej.

    ### Przykład:

    ```
    round(value: real): number
    ```

## Przykładowy skrypt

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
