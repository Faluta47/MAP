RETEA DE SOCIALIZARE - MAP Laborator 4 (refactorizare Java 8)
=============================================================

Functionalitati (neschimbate fata de Lab 3, date persistate in fisiere text): adaugare/stergere utilizator,
adaugare/stergere prietenie, numar de comunitati, cea mai sociabila comunitate. Singura adaugire vizibila:
optiunea de meniu "8. Cauta utilizatori" (demonstratie pentru findAll(Predicate)).
Lab 4 este un laborator de refactorizare: aplicatia face acelasi lucru ca la Lab 3 (tag git "lab3"), dar codul
este rescris cu interfete functionale, lambda, referinte la metode, Stream API si Optional (tag git "lab4").


1. CUM SE RULEAZA
-----------------
Datele se cauta relativ la directorul curent (data/...), deci aplicatia trebuie pornita din radacina proiectului.

IntelliJ:
  - deschide folderul ca proiect Gradle;
  - Run > Edit Configurations > Application, main class ro.ubb.map.retea.Main;
  - Working directory = radacina proiectului (folderul care contine data/ si src/).

Terminal, cu Gradle instalat local:   gradle run        (teste: gradle test)
Terminal, fara Gradle:
  javac -d out $(find src/main -name '*.java')
  cp src/main/resources/config.properties out/
  java -cp out ro.ubb.map.retea.Main
  (testele necesita jar-urile JUnit 5: javac -cp <jar-uri> -d out-test ... si rulare cu launcher-ul JUnit)

Configurare: src/main/resources/config.properties (se citeste din classpath).
  repository.type = MEMORY | FILE | DATABASE
  file.utilizatori, file.prietenii   (FILE)
  db.url, db.user, db.password       (DATABASE - schelet, se implementeaza la Lab 5)

JavaDoc: folderul docs/
  javadoc -d docs -sourcepath src/main/java -subpackages ro.ubb.map.retea

Verificare comportament neschimbat: folderul verificare/ contine outputul aceluiasi scenariu (stdin fix) rulat pe
datele normale si pe data/test/*_corupt.txt: output_lab3.txt / output_corupt_lab3.txt (tag lab3) si
output_lab4.txt / output_corupt_lab4.txt. Diferentele: linia noua "8. Cauta utilizatori" din meniu si data
prieteniei adaugate (LocalDateTime.now()).


2. FORMATUL FISIERELOR (neschimbat)
-----------------------------------
Fisiere text UTF-8, o entitate pe linie, campuri separate prin ";" (citite cu linie.split(";", -1)).
  data/utilizatori.txt:  id;prenume;nume            (sau id;prenume;nume;username)
  data/prietenii.txt:    idUtilizator1;idUtilizator2;dataPrieteniei
Data este in format ISO (LocalDateTime), de exemplu 2026-10-19T10:30. Prietenia (1,2) este aceeasi cu (2,1):
id-urile se normalizeaza (cel mai mic primul). Numele nu pot contine ";". Liniile goale se ignora.


3. TABEL INAINTE / DUPA (bucle inlocuite)
-----------------------------------------
Clasa.metoda                          Inainte (Lab 3)                         Dupa (Lab 4) - operatii stream
------------------------------------  --------------------------------------  ------------------------------------
UtilizatorServiceImpl.adaugaUtilizator for + if (maxim manual)                 stream().mapToLong(Entity::getId)
                                                                                .max().orElse(0)  [reduce-ul max]
UtilizatorServiceImpl.stergeUtilizator for peste prietenii + if + delete       findAll(Predicate) -> stream() ->
                                                                                map(Entity::getId) ->
                                                                                forEach(repoPrietenii::delete)
UtilizatorServiceImpl.getUtilizatori   2 for-uri imbricate + add in lista      stream().collect(toList()),
                                                                                collect(toMap), filter, forEach
PrietenieServiceImpl.getPrietenii      for + add intr-o lista exterioara       stream().collect(toList())
ReteaServiceImpl.incarcaUtilizatori    for + put in LinkedHashMap              stream().collect(toMap(Entity::getId,
                                                                                identity(), merge, LinkedHashMap::new))
ReteaServiceImpl.construiesteGraf      for + get/add pe listele de vecini      filter + flatMap(2 muchii/prietenie) +
                                                                                collect(groupingBy+mapping+toList)
ReteaServiceImpl.comunitati            for + visited exterior + dfs cu for     map(dfs) + distinct() + map + collect;
                                                                                dfs cu stream().filter().forEach()
ReteaServiceImpl.ceaMaiSociabila...    for peste componente, if mai bun        map(comunitateSociabila) +
                                                                                max(comparingInt(getLungimeDrum))
ReteaServiceImpl.celMaiLungDrum        backtracking cu for, add/remove         recursie cu stream: concat + filter +
                                                                                map + max(comparingInt(List::size))
FileRepository.ensureLoaded            for cu index + if                       IntStream.range + filter + forEach
FileRepository.readLines               BufferedReader + while readLine         Files.lines + collect(toList), in
                                                                                try-with-resources
FileRepository.writeData               BufferedWriter + for + write            stream().map(formatter).collect(toList())
                                                                                + Files.write
ConsoleUI.afiseazaUtilizatori / ...    for-each + println                      forEach(this::...), map + forEach(
                                                                                System.out::println), joining(" => ")
UsernameSearch.find                    for + if (sters)                        Predicate CriteriiUtilizator.areUsername
                                                                                + findAll(Predicate)


4. INTERFETE FUNCTIONALE FOLOSITE SI UNDE
-----------------------------------------
Validator<T>        @FunctionalInterface proprie, cu default andThen. Regula "numele are cel mult 30 de caractere"
                    este o lambda compusa in Main cu UtilizatorValidator (nemodificat). In teste: reguli lambda.
Predicate<T>        Repository.findAll(Predicate), CriteriiUtilizator (and, or, negate, Predicate.not),
                    UtilizatorService.cautaUtilizatori, filter(...) in service-uri si repository-uri.
Function<T,R>       FileRepository (parser Function<String,E>, formatter Function<E,String>); RepositoryFactory
                    (Function<Validator<E>, Repository<ID,E>> in EnumMap); map(...), Function.identity().
Supplier, BinaryOp. Supplier in orElseThrow(() -> new ...Exception(...)), BinaryOperator in toMap(merge)
                    si Predicate::and / Predicate::or in reduce.
Consumer<T>         forEach(...), Optional.ifPresent / ifPresentOrElse (Runnable pentru ramura "gol").
Comparator<T>       Comparator.comparingInt(...) in max(...) pentru comunitati si drumuri. Nu exista compare scris
                    de mana si nici scaderi (a, b) -> a - b.


5. REFERINTE LA METODE (cele 4 tipuri)
--------------------------------------
Metoda statica                   CriteriiUtilizator::numeIncepeCu, ::prenumeContine, UtilizatorConverter::fromLine,
                                 UtilizatorConverter::toLine, PrietenieConverter::fromLine/::toLine
Metoda de instanta a unui        System.out::println (ConsoleUI), repoPrietenii::delete (UtilizatorServiceImpl),
obiect anume                     utilizatori::get (ReteaServiceImpl), this::afiseazaUtilizator (ConsoleUI)
Metoda de instanta a unui        Entity::getId, ComunitateSociabila::getLungimeDrum, String::trim, String::isEmpty,
obiect oarecare                  List::size, Map.Entry::getKey
Constructor                      InMemoryRepository::new (RepositoryFactory), ArrayList::new (InMemoryRepository),
                                 LinkedHashMap::new, LinkedHashSet::new (ReteaServiceImpl)


6. ALEGERI DE PROIECTARE
------------------------
6.1 Optional in Repository (decizie: DA, Repository intoarce Optional<E>)
  findOne/delete intorc Optional<E> (gol daca nu exista), save/update intorc Optional<E> cu semnificatia
  veche a lui null: save -> gol daca s-a salvat, altfel entitatea existenta; update -> gol daca s-a actualizat,
  altfel entitatea data. Argumentele null arunca in continuare IllegalArgumentException.
  Justificare: "nu exista" devine vizibil in tip, apelantul este obligat sa decida (orElseThrow, ifPresent, map),
  dispar NullPointerException-urile uitate, iar rezultatul se leaga direct de stream-uri. Costul: toate
  implementarile, service-urile si testele s-au modificat consecvent (null -> Optional). Optional este folosit doar ca
  tip de retur (niciodata camp sau parametru). Service: UtilizatorService.gasesteUtilizator intoarce
  Optional<Utilizator>; ReteaService.ceaMaiSociabilaComunitate intoarce Optional<ComunitateSociabila> (gol daca nu
  exista utilizatori). Nu exista apel Optional.get() fara verificare.
  Consecinta: UserRepository/findByUsername au disparut (cerinta: fara metode de tip findBy... in repository);
  cautarea dupa username este CriteriiUtilizator.areUsername(...) cu findAll(Predicate).

6.2 Template Method vs. Function in repository-urile pe fisier (decizie: FileRepository<ID,E> cu Function)
  Inainte: AbstractFileRepository (Template Method) + UtilizatorFileRepository + PrietenieFileRepository, care
  extindeau InMemoryRepository si suprascriau extractEntity/createEntityAsString.
  Acum: o singura clasa FileRepository<ID,E>(cale, validator, Function<String,E> parser, Function<E,String>
  formatter, LoadWarnings) care FOLOSESTE un InMemoryRepository (compozitie), nu il extinde. Conversiile sunt metode
  statice in UtilizatorConverter / PrietenieConverter, date factory-ului ca referinte la metode.
  + doua clase mai putin si nicio ierarhie: un format nou = un converter, nu o subclasa;
  + conversia se testeaza si se refoloseste separat de fisier (parser/formatter sunt functii pure);
  + fara apeluri de metode suprascriibile (nu mai e nevoie de grija din Effective Java, Item 19);
  + compozitie in loc de mostenire: FileRepository nu mosteneste metodele lui InMemoryRepository, deci nu poate
    ocoli din greseala scrierea in fisier (in Lab 3 save/delete/update erau suprascrise tocmai pentru asta);
  - Template Method garanta la compilare ca subclasa implementeaza ambele metode; acum acest lucru este garantat de
    constructorul cu doi parametri Function, deci echivalent;
  - parametrii functionali sunt mai multi (constructor mai lung).

6.3 RepositoryFactory fara switch
  Un EnumMap<RepositoryType, Function<Validator<E>, Repository<ID,E>>> pentru utilizatori si altul pentru
  prietenii. Se ruleaza doar functia tipului ales, deci proprietatile celorlalte tipuri nu sunt citite (MEMORY nu
  cere file.* sau db.*). Tipul lipsa/necunoscut: ConfigException cu mesaj clar (neschimbat).
  OCP: tip nou = intrare noua in map (nu mai exista case-uri de modificat in doua metode).

6.4 Validator.andThen
  Compunerea ruleaza regulile in ordine si se opreste la prima care arunca; UtilizatorValidator ramane nemodificat si isi
  aduna in continuare toate erorile proprii intr-un mesaj.

6.5 Comunitati
  comunitati(): se porneste un dfs (pur din exterior: multimea vizitata este locala apelului) din fiecare utilizator;
  componentele aceleiasi comunitati dau multimi egale, iar distinct() o pastreaza pe prima, cea pornita din primul
  membru - exact ordinea si pre-ordinea DFS de la Lab 3 (verificat pe 60 de grafuri aleatoare, vezi punctul 12).
  Cea mai sociabila: max(Comparator.comparingInt(getLungimeDrum)) peste comunitati. Implementarea OpenJDK a lui
  Stream.max pastreaza primul element dintre cele egale, deci la egalitate ramane prima gasita, ca la Lab 3
  (testele ceaMaiSociabilaLaEgalitateEPrima si ...EPrimaGasita). Cautarea celui mai lung drum foloseste liste
  imutabile in loc de lista comuna modificata si restaurata (add/remove).


7. BUCLE PASTRATE
-----------------
Singura bucla ramasa in domain/service/repository/ui: while (mergeMai) din ConsoleUI.run() (bucla meniului).
Nu parcurge o colectie, ci repeta citirea comenzilor pana la "0" sau pana se termina intrarea. Backtracking-ul pentru
cel mai lung drum a putut fi scris recursiv cu stream-uri (celMaiLungDrum), deci nu s-a mai pastrat nicio bucla pentru el.
In teste nu mai exista try/fail: se foloseste assertThrows cu lambda.
Efecte secundare: forEach/ifPresent sunt operatii terminale folosite pentru a modifica obiecte (listele de prieteni din
getUtilizatori, scrierea in repository la incarcare, stergerea prieteniilor); functiile date lui filter si map nu
modifica nimic din exteriorul stream-ului.
Diferenta de comportament cunoscuta: Files.lines(UTF_8) arunca la un fisier cu bytes UTF-8 invalizi (devine
RepositoryException), pe cand FileReader de la Lab 3 inlocuia silentios caracterele invalide.


8. FISIER LIPSA SI LINII CORUPTE (neschimbat)
---------------------------------------------
Fisier lipsa: repository-ul porneste gol, fara eroare; fisierul (si folderul data/) se creeaza la prima salvare.
Incarcarea este lenesa: constructorul nu citeste nimic; ensureLoaded() ruleaza la inceputul fiecarei operatii.
Se citeste tot fisierul (Files.lines intr-un try-with-resources, colectat intr-o lista) inainte sa se modifice ceva in
memorie; liniile se parcurg cu IntStream.range(0, linii.size()) ca sa se pastreze numarul liniei.
Linie corupta (prea putine campuri, id nenumeric, data gresita, entitate invalida, id duplicat, prietenie cu utilizator
inexistent): este sarita si raportata ca avertisment "fisier, linia N: motiv (continut: ...)". Avertismentele ajung la UI
prin LoadWarnings -> ReteaService.getAvertismenteIncarcare().
Exceptii: IOException (si UncheckedIOException din Files.lines) -> RepositoryException; NumberFormatException si
DateTimeParseException -> RepositoryException (din converter). UI-ul afiseaza mesajul, fara stack trace.


9. UNDE SE FACE VALIDAREA
-------------------------
Validatorul se aplica in repository (la save/update si la incarcarea din fisier), service-ul face regulile de business
(prietenie duplicata, utilizator inexistent, stergere in cascada, generare id). PrietenieValidator primeste repository-ul
de utilizatori si verifica ca ambii utilizatori exista (si la incarcare, cu numarul liniei).
Stergerea unui utilizator sterge intai prieteniile lui, apoi utilizatorul.


10. SOLID SI SABLOANE
---------------------
DIP  UI, service-urile si Main depind de interfete (UI, *Service, Repository, Validator).
OCP  Tip nou de persistenta = intrare in EnumMap-urile din RepositoryFactory + o clasa Repository; criteriu nou de
     cautare = o lambda/metoda statica Predicate, fara a atinge repository-ul; regula noua = Validator.andThen.
SRP  FileRepository (citeste/scrie fisierul), Converter-ele (linie <-> entitate), RepositoryFactory (creeaza),
     LoadWarnings (aduna mesaje), ConsoleUI (interactiune), ReteaServiceImpl (graful).
LSP  InMemoryRepository, FileRepository si *DbRepository respecta acelasi contract (Optional, null -> IAE);
     exceptie declarata: scheletele DB arunca UnsupportedOperationException (mesajul a ramas "(Lab 4)" ca sa nu se schimbe
     outputul; DB se implementeaza la Lab 5).
ISP  Interfete mici: Validator (o metoda abstracta), UI, Repository (CRUD + 2 metode default), trei service-uri.
Strategy: Validator, Predicate (criterii), Function (parser/formatter), Repository (persistenta). Simple Factory cu
tabel de creatori: RepositoryFactory. Decorator: BufferedReader/Writer nu mai apar (Files.lines/write). Dependency
Injection: Main construieste obiectele. Comparatie Simple vs. Abstract Factory: ca la Lab 3, Simple Factory ajunge
pentru doua produse; harta de creatori elimina switch-ul fara clase noi.


11. STRUCTURA PE STRATURI (ro.ubb.map.retea)
--------------------------------------------
  domain      Entity, Tuple, Utilizator, Prietenie
  validators  Validator<T> (functionala, andThen), UtilizatorValidator, PrietenieValidator
  exceptions  ValidationException, RepositoryException, ServiceException, EntityNotFoundException, ConfigException
  repository  Repository (Optional, stream(), findAll(Predicate)), CriteriiUtilizator, LoadWarnings
              memory/InMemoryRepository
              file/FileRepository, UtilizatorConverter, PrietenieConverter
              db/UtilizatorDbRepository, PrietenieDbRepository (schelete)
              factory/RepositoryFactory, RepositoryType
  service     UtilizatorService, PrietenieService, ReteaService + *Impl, ComunitateSociabila
  ui          UI, ConsoleUI
  Main        incarca configurarea, construieste obiectele (inclusiv regula lambda), porneste UI-ul
Dependentele merg intr-un singur sens: ui -> service -> repository -> domain.


12. DEMONSTRATIA LA PREDARE
---------------------------
a) Aplicatia ruleaza cu aceleasi rezultate: diff verificare/output_lab3.txt verificare/output_lab4.txt (si variantele
   _corupt). Pentru regula "prima gasita" vezi testele din ServiceTest. Echivalenta a fost verificata si pe 60 de grafuri
   aleatoare (aceleasi fisiere date ambelor versiuni, acelasi output la optiunile 5, 6, 7).
b) Trei locuri inainte/dupa (git diff lab3 lab4 -- <fisier>):
   1. UtilizatorServiceImpl.stergeUtilizator
        git diff lab3 lab4 -- src/main/java/ro/ubb/map/retea/service/UtilizatorServiceImpl.java
      Pipeline: findAll(Predicate) (colecteaza lista prieteniilor utilizatorului) -> stream() [sursa]
      -> map(Entity::getId) [intermediara] -> forEach(repoPrietenii::delete) [terminala]. Nimic nu ruleaza pana la
      forEach (evaluare lenesa); lista e colectata inainte, deci stergerea nu strica parcurgerea.
   2. FileRepository (citire/scriere)
        git diff lab3:src/main/java/ro/ubb/map/retea/repository/file/AbstractFileRepository.java \
                 lab4:src/main/java/ro/ubb/map/retea/repository/file/FileRepository.java
      Citire: Files.lines(path) [sursa, tine fisierul deschis -> try-with-resources] -> collect(toList()) [terminala].
      Incarcare: IntStream.range(0, n) -> filter(linie nevida) [intermediara] -> forEach(incarcaLinie) [terminala].
      Scriere: stream().map(formatter) [intermediara] -> collect(toList()) [terminala] -> Files.write.
   3. ReteaServiceImpl.comunitati / ceaMaiSociabilaComunitate
        git diff lab3 lab4 -- src/main/java/ro/ubb/map/retea/service/ReteaServiceImpl.java
      Pipeline: keySet().stream() -> map(id -> dfs(...)) [intermediara] -> distinct() [intermediara, cu stare]
      -> map(membri) [intermediara] -> collect(toList()) [terminala]. Cea mai sociabila: map(comunitateSociabila)
      [intermediara] -> max(comparingInt(getLungimeDrum)) [terminala, intoarce Optional] -> ifPresentOrElse in UI.
c) Regula de validare noua doar prin lambda (in Main, la construirea validatorului):
      new UtilizatorValidator().andThen(u -> { if (u.getPrenume().trim().length() > 20) throw new ValidationException("..."); })
d) Criteriu de cautare nou doar prin lambda (fara clase noi), de exemplu in ConsoleUI/test:
      Predicate<Utilizator> c = CriteriiUtilizator.numeIncepeCu("Po").and(u -> u.getPrenume().startsWith("I"));
      utilizatorService.cautaUtilizatori(c).forEach(System.out::println);
   sau din meniu, optiunea 8 (prefixe de nume separate prin virgula = or; celelalte raspunsuri = and).


13. DATE DE DEMONSTRATIE
------------------------
data/utilizatori.txt: 10 utilizatori. data/prietenii.txt: 3 comunitati - un lant 1-2-3-4-5 (drum de 4 muchii, cea mai
sociabila), o comunitate densa {6,7,8,9} (graf complet) si utilizatorul izolat 10.
data/test/: utilizatori_corupt.txt si prietenii_corupt.txt - cate o linie buna si cate un exemplu din fiecare tip de
linie corupta.
