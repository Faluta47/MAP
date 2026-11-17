RETEA DE SOCIALIZARE - MAP Laborator 5 (persistenta in PostgreSQL cu JDBC; peste Lab 4)
========================================================================================

Lab 5 nu adauga functionalitati: aceleasi operatii, dar datele pot sta intr-o baza PostgreSQL, iar id-ul
utilizatorilor este dat de STOCARE, nu de service (sectiunile 14-23 de la finalul fisierului). Sectiunile 1-13 sunt
cele de la Lab 4, actualizate acolo unde Lab 5 le-a schimbat. Tag-uri git: lab3, lab4, lab5.

Lab 4 (refactorizare Java 8)

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
  javac -d out -cp postgresql-42.7.7.jar $(find src/main -name '*.java')
  cp src/main/resources/config.properties out/
  java -cp out:postgresql-42.7.7.jar ro.ubb.map.retea.Main
  (driverul PostgreSQL trebuie pus pe classpath doar pentru DATABASE; pentru FILE/MEMORY nu este folosit)
  (testele necesita jar-urile JUnit 5: javac -cp <jar-uri> -d out-test ... si rulare cu launcher-ul JUnit)

Configurare: src/main/resources/config.properties (se citeste din classpath; NU este in Git, are parola bazei de date).
  Se creeaza din src/main/resources/config.example.properties (vezi sectiunea 14).
  repository.type = MEMORY | FILE | DATABASE
  file.utilizatori, file.prietenii   (FILE)
  db.url, db.user, db.password       (DATABASE)

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
id-urile se normalizeaza (cel mai mic primul). In modul FILE numele nu pot contine ";" (regula este in
UtilizatorConverter.toLine, vezi 19). Liniile goale se ignora.


3. TABEL INAINTE / DUPA (bucle inlocuite)
-----------------------------------------
Clasa.metoda                          Inainte (Lab 3)                         Dupa (Lab 4) - operatii stream
------------------------------------  --------------------------------------  ------------------------------------
UtilizatorServiceImpl.adaugaUtilizator for + if (maxim manual)                 (Lab 5: id-ul il da repository-ul;
                                                                                IdGenerator.maxPlusOne: mapToLong.max)
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
ConsoleUI.cautaUtilizatori             (nou in Lab 4)                          Predicate and/or/negate + sorted(
                                                                                comparing.thenComparing[.reversed()])


4. INTERFETE FUNCTIONALE FOLOSITE SI UNDE
-----------------------------------------
Validator<T>        @FunctionalInterface proprie, cu default andThen. Regula "numele are cel mult 30 de caractere"
                    este o lambda compusa in Main cu UtilizatorValidator (nemodificat). In teste: reguli lambda.
Predicate<T>        Repository.findAll(Predicate), CriteriiUtilizator (and, or, negate, Predicate.not),
                    UtilizatorService.cautaUtilizatori, filter(...) in service-uri si repository-uri.
Function<T,R>       FileRepository (parser Function<String,E>, formatter Function<E,String>); RepositoryFactory
                    (Function<Validator<E>, Repository<ID,E>> in EnumMap); map(...), Function.identity().
BinaryOperator    Supplier in orElseThrow(() -> new ...Exception(...)); BinaryOperator in toMap(merge),
                    Predicate::or in reduce (CriteriiUtilizator) si reduce-ul din ReteaServiceImpl.dfs.
Consumer<T>         forEach(...), Optional.ifPresent / ifPresentOrElse (Runnable pentru ramura "gol").
Comparator<T>       comparingInt in max(...) pentru comunitati si drumuri; comparing(Utilizator::getNume)
                    .thenComparing(getPrenume) [.reversed()] in ConsoleUI.cautaUtilizatori (sorted). Nu exista compare scris
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
  comunitati(): se porneste un dfs fara stare partajata (fiecare apel intoarce o multime noua; vecinii se
  parcurg cu reduce, acumulatorul fiind multimea vizitata pana atunci, deci filtrul nu depinde de efectele altui pas) din fiecare utilizator;
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
(prietenie duplicata, utilizator inexistent, stergere in cascada). Din Lab 5 id-ul NU mai e generat de service. PrietenieValidator primeste repository-ul
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
     (la Lab 5 repository-urile DB au inlocuit scheletele si respecta acelasi contract).
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
              db/UtilizatorDbRepository, PrietenieDbRepository, AbstractDbRepository, SqlStateMessages (JDBC)
              IdGenerator (id generat in repository pentru MEMORY/FILE)
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
      new UtilizatorValidator().andThen(u -> { if (u.getNume().trim().length() > 30) throw new ValidationException("..."); })
d) Criteriu de cautare nou doar prin lambda (fara clase noi), de exemplu in ConsoleUI/test:
      Predicate<Utilizator> c = CriteriiUtilizator.numeIncepeCu("Po").and(u -> u.getPrenume().startsWith("I"));
      utilizatorService.cautaUtilizatori(c).forEach(System.out::println);
   sau din meniu, optiunea 8 (prefixe de nume separate prin virgula = or; celelalte raspunsuri = and; "cu prieteni" =
   negate; rezultatele sunt sortate dupa nume si prenume, optional descrescator).


13. DATE DE DEMONSTRATIE
------------------------
data/utilizatori.txt: 10 utilizatori. data/prietenii.txt: 3 comunitati - un lant 1-2-3-4-5 (drum de 4 muchii, cea mai
sociabila), o comunitate densa {6,7,8,9} (graf complet) si utilizatorul izolat 10.
data/test/: utilizatori_corupt.txt si prietenii_corupt.txt - cate o linie buna si cate un exemplu din fiecare tip de
linie corupta.


=====================================================================
LABORATOR 5 - PERSISTENTA IN POSTGRESQL (JDBC)
=====================================================================

14. CUM SE RULEAZA SI CUM SE CONFIGUREAZA BAZA DE DATE
------------------------------------------------------
1) In pgAdmin (sau psql), conectat ca superuser/owner, creeaza baza (nu se poate intr-o tranzactie, deci nu e in script):
       CREATE DATABASE socialnetwork;
2) Conecteaza-te la socialnetwork si ruleaza, in ordine: sql/schema.sql, apoi sql/seed.sql
   (seed.sql pune cei 10 utilizatori, cu id-urile 1..10, si cele 10 prietenii din data/). Reset: sql/drop.sql.
3) Copiaza src/main/resources/config.example.properties in src/main/resources/config.properties si completeaza
   db.user / db.password (si db.url daca serverul nu e pe localhost:5432). config.properties este in .gitignore
   si scos din index: parola nu ajunge niciodata in Git.
4) Alege tipul de persistenta DOAR din config.properties: repository.type=DATABASE (sau FILE / MEMORY). Fara
   modificari de cod. Cheie lipsa sau tip necunoscut: ConfigException cu mesaj clar.
5) Gradle (build.gradle.kts): driverul org.postgresql:postgresql:42.7.7 este dependinta "implementation".
       gradle run        gradle test
Teste de integrare pe baza de date: ruleaza doar daca sunt setate TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD
(altfel sunt sarite). Ele isi creeaza o schema temporara proprie (lab5_test_<uuid>) si o sterg la final; nu ating
tabelele users/friendships din socialnetwork. Exemplu:
       TEST_DB_URL=jdbc:postgresql://localhost:5432/socialnetwork TEST_DB_USER=postgres TEST_DB_PASSWORD=... gradle test


15. SCHEMA SI STERGEREA IN CASCADA
----------------------------------
users(id BIGINT GENERATED ALWAYS AS IDENTITY PK, first_name NOT NULL, last_name NOT NULL)
friendships(id1, id2, friends_from TIMESTAMP NOT NULL, PK(id1,id2), CHECK(id1 < id2),
            FK id1/id2 -> users(id) ON DELETE CASCADE)
CHECK(id1 < id2) se potriveste cu domeniul: Prietenie normalizeaza deja perechea (id-ul mai mic primul).
Stergerea prieteniilor: UtilizatorServiceImpl.stergeUtilizator le sterge EXPLICIT (apoi sterge utilizatorul), iar
ON DELETE CASCADE ramane ca plasa de siguranta. Motiv: logica de business sa fie aceeasi pentru MEMORY, FILE si
DATABASE (fisierul si memoria nu au cascada; fara stergerea explicita ar ramane prietenii orfane), iar cascada
protejeaza baza daca un utilizator e sters direct din SQL sau din alta aplicatie (testul cascadaDinBaza... din
DbRepositoryTest verifica asta). Costul: cateva DELETE-uri in plus, in operatii separate (fara tranzactie comuna).


16. CUM SE GENEREAZA ID-UL (SI DE CE IN REPOSITORY, NU IN SERVICE)
-------------------------------------------------------------------
DATABASE  id-ul vine din baza: INSERT INTO users(first_name, last_name) VALUES (?, ?) RETURNING id; valoarea se pune
          in entitate. Un id pus de apelant este ignorat si suprascris (documentat in JavaDoc la save).
MEMORY    InMemoryRepository primeste optional un IdGenerator (interfata functionala); RepositoryFactory ii da
FILE      IdGenerator.maxPlusOne() pentru utilizatori si nimic pentru prietenii (id-ul lor este perechea). Cand
          entitatea nu are id se pune max+1 (ca la Lab 4, deci aceleasi id-uri). Id-urile din fisier se pastreaza.
Motiv: "cine da id-ul" este o proprietate a stocarii (o baza il da singura, iar max+1 in service ar fi o cursa
intre doua aplicatii); astfel service-ul are acelasi cod pentru toate cele trei tipuri si nu mai stie cum se
numeroteaza. UtilizatorValidator accepta id null (id-ul exista abia dupa salvare). Dupa save, service-ul intoarce
utilizatorul cu id-ul primit, iar ConsoleUI scrie "Utilizator adaugat cu id X: ...".
Limita: coloana username a lui Utilizator nu exista in schema ceruta, deci in DATABASE nu se pastreaza (in FILE da).


17. DE CE PreparedStatement (SQL INJECTION) SI NUMELE DE DEMONSTRATIE
---------------------------------------------------------------------
Toate interogarile sunt PreparedStatement cu parametri "?"; valorile nu se concateneaza niciodata in textul SQL
(nu exista Statement simplu in src/main). Numele '); DROP TABLE users; -- ajunge ca parametru, deci e DATA, nu
cod SQL: se salveaza ca text obisnuit (SELECT * FROM users WHERE last_name LIKE '%DROP%' il arata) si tabela users
ramane. Nu il respinge nici validatorul, nici regula lambda din Main (limita de 30 de caractere; sirul are 24).
Conexiunea se deschide pe operatie (DriverManager.getConnection(url, user, password)) in try-with-resources,
fara conexiune tinuta in camp.


18. SQLState -> MESAJ IN UI (SqlStateMessages, testabila fara DB)
------------------------------------------------------------------
SQLException se impacheteaza in RepositoryException (cu cauza); nicio SQLException nu iese din repository.
UI-ul afiseaza doar mesajul, fara stack trace; pornirea (incarcarea initiala) si rularea cu serverul oprit nu
opresc aplicatia: se afiseaza mesajul si se revine la meniu. (Mesajele sunt fara diacritice, ca restul UI-ului.)
  08xxx (conexiune, server oprit)  "Baza de date nu este disponibila. Verifica serverul PostgreSQL si configurarea (db.url)."
  28xxx (autentificare)            "Autentificare esuata la baza de date: user sau parola gresita (verifica db.user si db.password)."
  23503 (cheie straina)            "Utilizator inexistent: prietenia trebuie sa lege doi utilizatori existenti."
  23505 (cheie duplicata)          "Prietenia exista deja."
  23514 (CHECK)                    "Prietenia este invalida: se cere id1 < id2."
  altele                           "Eroare la accesarea bazei de date (<SQLState>): <motivul din cauza>"
Prietenia duplicata prin save nu ajunge la 23505: INSERT ... ON CONFLICT (id1, id2) DO NOTHING si 0 randuri
inserate inseamna "exista deja" -> save intoarce Optional.of(entitatea), conform contractului. 23505 ramane mapat
pentru orice alt caz de cheie duplicata.


19. DECIZIA PENTRU ";" (VALIDARE MUTATA IN FISIER)
--------------------------------------------------
";" era interzis in UtilizatorValidator doar din cauza formatului fisierului (separator). Regula a fost scoasa din
validator (in DATABASE un nume cu ";" e valid) si mutata in UtilizatorConverter.toLine: in modul FILE, un nume cu ";"
da RepositoryException("numele nu poate fi salvat in fisier: contine ;"). FileRepository nu strica fisierul si nici
memoria: scrierea esueaza inainte de Files.write, iar entitatea e scoasa din memorie (la update se restaureaza
versiunea veche). Interdictia pentru sfarsit de linie a ramas in validator.


20. BUCLA while (rs.next()) PASTRATA
------------------------------------
UtilizatorDbRepository.findAll si PrietenieDbRepository.findAll parcurg un ResultSet cu while (rs.next()). Este o
exceptie justificata de la regula "fara bucle clasice": ResultSet nu e o colectie, ci un cursor care avanseaza
doar cu next() si nu poate fi transformat in Stream fara a construi un iterator artificial. Restul codului ramane in
stil cu stream-uri / Optional. Bucle ramase in src/main: while (mergeMai) din ConsoleUI + cele doua while (rs.next()).


21. CE S-A SCHIMBAT FATA DE LAB 4  (git diff lab4 lab5 --stat -- src)
-----------------------------------------------------------------------
   .../ro/ubb/map/retea/repository/IdGenerator.java   |  34 +++
   .../retea/repository/db/AbstractDbRepository.java  |  73 +++++
   .../retea/repository/db/PrietenieDbRepository.java | 137 ++++++---
   .../map/retea/repository/db/SqlStateMessages.java  |  47 +++
   .../repository/db/UtilizatorDbRepository.java      | 141 ++++++---
   .../repository/factory/RepositoryFactory.java      |   7 +-
   .../map/retea/repository/file/FileRepository.java  |  43 ++-
   .../retea/repository/file/UtilizatorConverter.java |  13 +-
   .../repository/memory/InMemoryRepository.java      |  24 +-
   .../ubb/map/retea/service/UtilizatorService.java   |   3 +-
   .../map/retea/service/UtilizatorServiceImpl.java   |  12 +-
   src/main/java/ro/ubb/map/retea/ui/ConsoleUI.java   |   2 +-
   .../map/retea/validators/UtilizatorValidator.java  |  13 +-
   src/main/resources/config.example.properties       |  12 +
   src/main/resources/config.properties               |  11 -
   .../java/ro/ubb/map/retea/DbRepositoryTest.java    | 317 ++++++++++++++++-----
   .../java/ro/ubb/map/retea/IdGenerationTest.java    | 160 +++++++++++
   .../ro/ubb/map/retea/RepositoryFactoryTest.java    |  20 ++
   src/test/java/ro/ubb/map/retea/ServiceTest.java    |   6 +-
   .../ro/ubb/map/retea/SqlStateMessagesTest.java     |  57 ++++
   src/test/java/ro/ubb/map/retea/ValidatoriTest.java |  18 +-
   21 files changed, 962 insertions(+), 188 deletions(-)

(Tot in repo: sql/schema.sql, sql/seed.sql, sql/drop.sql, build.gradle.kts + settings.gradle.kts in locul celor
Groovy, .gitignore pentru config.properties, verificare/output_lab4_final.txt, docs/ regenerat.)


22. SCENARIUL DE DEMONSTRATIE (pas cu pas)
------------------------------------------
 1. pgAdmin: CREATE DATABASE socialnetwork; ruleaza schema.sql si seed.sql; SELECT * FROM users; SELECT * FROM friendships;
 2. config.properties: repository.type=DATABASE, db.user/db.password reale. Porneste aplicatia.
 3. Optiunea 5: se vad cei 10 utilizatori si prieteniile lor, citite din baza.
 4. Optiunea 1 (Test / Nou): apare "Utilizator adaugat cu id 11". In pgAdmin: SELECT * FROM users; -> randul 11.
 5. Optiunea 3 (1 si 5): prietenie adaugata; in pgAdmin SELECT * FROM friendships WHERE id1 = 1; (id1 < id2 respectat).
 6. Optiunea 3 (1 si 5) din nou: "Eroare: Prietenia exista deja."
 7. Optiunea 2 (id 1): utilizatorul se sterge; SELECT * FROM friendships WHERE id1 = 1 OR id2 = 1; -> 0 randuri.
 8. Optiunea 6: numarul de comunitati; optiunea 7: cea mai sociabila.
 9. Optiunea 0, repornire, optiunea 5: datele sunt pastrate.
10. Optiunea 1 cu numele '); DROP TABLE users; -- : se salveaza cu id nou; in pgAdmin SELECT * FROM users; arata numele
    ca text, iar tabela users exista in continuare (injectia nu se executa).
11. Opreste serverul PostgreSQL (sau pune alta parola in config) si porneste aplicatia / alege o optiune: mesajul
    "Baza de date nu este disponibila..." (sau cel de autentificare), fara stack trace, aplicatia ramane in meniu.
12. repository.type=FILE, apoi MEMORY, doar din config.properties, fara alte modificari: aceleasi functionalitati.


23. VERIFICARE
--------------
verificare/output_lab4_final.txt = scenariul fix rulat in modul FILE la Lab 4; rularea Lab 5 in FILE difera doar prin
mesajul nou "Utilizator adaugat cu id 11: #11 Test Nou" si prin data prieteniei adaugate (LocalDateTime.now()).
