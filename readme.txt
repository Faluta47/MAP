RETEA DE SOCIALIZARE - MAP Laborator 3
=======================================

Functionalitati (neschimbate fata de Lab 2, dar cu date persistate in fisiere text):
adaugare/stergere utilizator, adaugare/stergere prietenie, numar de comunitati,
cea mai sociabila comunitate.


1. CUM SE RULEAZA
-----------------
Datele se cauta relativ la directorul curent (data/...), deci aplicatia trebuie pornita din
radacina proiectului.

IntelliJ:
  - deschide folderul ca proiect Gradle;
  - Run > Edit Configurations > Application, main class ro.ubb.map.retea.Main;
  - Working directory = radacina proiectului (folderul care contine data/ si src/). Fara asta,
    fisierele data/... nu sunt gasite si repository-ul porneste gol.

Terminal, cu Gradle (daca exista wrapper-ul sau o instalare locala):
  ./gradlew run        (teste: ./gradlew test)
Terminal, fara Gradle:
  javac -d out $(find src/main -name '*.java')
  cp src/main/resources/config.properties out/
  java -cp out ro.ubb.map.retea.Main
  (testele necesita jar-urile JUnit 5: javac -cp <jar-uri> -d out-test ... si rulare cu launcher-ul JUnit)

Configurare: src/main/resources/config.properties (se citeste din classpath; dupa modificare
trebuie recompilat/recopiat in out/ sau build/).
  repository.type = MEMORY | FILE | DATABASE
  file.utilizatori, file.prietenii   (FILE)
  db.url, db.user, db.password       (DATABASE)

JavaDoc: folderul docs/
  javadoc -d docs -sourcepath src/main/java -subpackages ro.ubb.map.retea


2. FORMATUL FISIERELOR
----------------------
Fisiere text UTF-8, o entitate pe linie, campuri separate prin ";" (citite cu linie.split(";", -1)).
  data/utilizatori.txt:  id;prenume;nume
  data/prietenii.txt:    idUtilizator1;idUtilizator2;dataPrieteniei
Data este in format ISO (LocalDateTime), de exemplu 2026-10-19T10:30 sau 2026-10-19T10:30:00.
Prietenia (1,2) este aceeasi cu (2,1): id-urile se normalizeaza (cel mai mic primul).
Numele nu pot contine ";" (validatorul le respinge, ca sa nu strice formatul liniei).
Liniile goale se ignora. Fisierele de test cu linii corupte: data/test/*_corupt.txt.


3. FACTORY-UL SI TRECEREA LA BAZA DE DATE
-----------------------------------------
RepositoryFactory (Simple Factory) este singurul loc care face "new" pe repository-uri concrete.
Citeste repository.type si creeaza, printr-un switch clasic, perechea de repository-uri:
  MEMORY    -> InMemoryRepository (pentru utilizatori si pentru prietenii)
  FILE      -> UtilizatorFileRepository + PrietenieFileRepository
  DATABASE  -> UtilizatorDbRepository + PrietenieDbRepository (schelete, repository.db)
Ambele metode (createUtilizatorRepository, createPrietenieRepository) folosesc acelasi tip de
persistenta. Valoare lipsa sau necunoscuta -> ConfigException cu mesaj clar (cheia si valoarea);
Main o prinde si o afiseaza.

Pentru trecerea la baza de date (Lab 4):
  1. se implementeaza metodele din UtilizatorDbRepository si PrietenieDbRepository (JDBC);
  2. se adauga driverul in build.gradle;
  3. se pune repository.type=DATABASE si db.url/db.user/db.password in config.properties.
Service-urile, UI-ul si Main raman neschimbate: ele cunosc doar interfata Repository.
Cu DATABASE in starea actuala, UI-ul afiseaza "Operatie indisponibila: ... not implemented yet (Lab 4)",
fara stack trace.


4. FISIER LIPSA SI LINII CORUPTE
--------------------------------
Fisier lipsa: repository-ul porneste gol, fara eroare. Fisierul (si folderul data/) se creeaza la
prima salvare. Justificare: la prima rulare nu exista date, iar aceasta nu e o eroare.

Incarcarea este lenesa: constructorul nu citeste nimic (Effective Java, Item 19 - nu se apeleaza
metode suprascriibile din constructor); ensureLoaded() ruleaza la inceputul fiecarei operatii publice.
La incarcare se foloseste super.save(...), deci fisierul nu e rescris in timpul citirii.
Fisierul se citeste integral inainte sa se modifice ceva in memorie: daca citirea esueaza
(IOException -> RepositoryException), repository-ul ramane neincarcat, nu pe jumatate incarcat.

Linie corupta (prea putine campuri, id nenumeric, data gresita, entitate invalida dupa validator,
id duplicat, prietenie cu utilizator inexistent): linia este sarita, restul fisierului se incarca,
iar aplicatia nu se opreste. Motivul se pastreaza ca avertisment: "fisier, linia N: motiv (continut: ...)".
Alegerea (raportare + ignorare in loc de oprire cu eroare) permite sa lucrezi cu un fisier
editat de mana, fara sa pierzi tot din cauza unei singure linii.

Cum ajunge avertismentul la utilizator, fara printari din service:
  - repository-ul doar ADAUGA mesaje intr-un obiect LoadWarnings (colector pasiv, fara I/O);
  - Main creeaza colectorul si il da factory-ului (care il pasa repository-urilor de fisier) si lui
    ReteaServiceImpl;
  - ReteaService.getAvertismenteIncarcare() forteaza incarcarea (utilizatori, apoi prietenii) si intoarce lista;
  - ConsoleUI o apeleaza la pornire si afiseaza avertismentele.
Astfel nici repository-ul, nici service-ul nu scriu in consola, iar UI-ul depinde doar de interfata ReteaService.
Atentie: liniile corupte raman in fisier pana la prima scriere; dupa ea fisierul este rescris doar cu
entitatile valide din memorie (liniile ignorate dispar). Fisierele din data/test/ nu se modifica daca nu se
salveaza nimic.

Exceptiile: RepositoryException impacheteaza IOException, NumberFormatException si
DateTimeParseException (exception chaining). Repository-ul prinde doar ca sa impacheteze sau ca sa
transforme o linie rea in avertisment; UI-ul prinde ValidationException, ServiceException,
EntityNotFoundException, RepositoryException si UnsupportedOperationException si afiseaza mesajul.


5. UNDE SE FACE VALIDAREA (REPOSITORY vs. SERVICE)
--------------------------------------------------
Regula aleasa: validatorul (Validator<T>) se aplica in repository (la save/update si la incarcarea din fisier),
iar service-ul face regulile de business.
  - Repository + validator: invariantele entitatii (campuri nevide, id-uri diferite, utilizatori existenti)
    trebuie sa tina pe ORICE cale de scriere si mai ales la incarcarea din fisier, care nu trece prin
    service. Daca validarea ar fi doar in service, un fisier editat de mana ar introduce date invalide.
  - PrietenieValidator primeste repository-ul de utilizatori si verifica ca ambii utilizatori exista;
    de aceea "o prietenie se poate salva doar intre utilizatori existenti" are loc si la save, si la incarcare
    (cu numarul liniei in avertisment).
  - Service: reguli care implica mai multe entitati sau o decizie de business - prietenie duplicata
    (ServiceException), utilizator inexistent semnalat ca EntityNotFoundException (mesaj mai precis decat cel
    de validare), stergerea in cascada a prieteniilor, generarea id-ului.
Consistenta: UtilizatorServiceImpl.stergeUtilizator sterge intai prieteniile utilizatorului (fiecare stergere
rescrie prietenii.txt), apoi utilizatorul (utilizatori.txt). Un esec la mijloc nu lasa prietenii spre un
utilizator inexistent.


6. SIMPLE FACTORY vs. ABSTRACT FACTORY (OCP)
--------------------------------------------
Simple Factory (folosit): o singura clasa cu o metoda per produs si un switch pe tip.
  + simplu, usor de citit, un singur loc pentru "new";
  - OCP nu e respectat complet: un tip nou (ex. REST) cere modificarea clasei si a switch-urilor
    (modificare, nu extindere). Nimic nu garanteaza ca cele doua metode aleg acelasi tip - aici o
    garanteaza doar faptul ca citesc aceeasi proprietate.
Abstract Factory: o interfata RepositoryFamilyFactory cu createUtilizatorRepository/createPrietenieRepository,
implementata de MemoryFactory, FileFactory, DbFactory.
  + respecta OCP: un tip nou = o clasa noua, fara sa umbli la cele existente; garanteaza ca produsele
    unei familii merg impreuna (aceeasi persistenta);
  - mai multe clase si o interfata in plus; tot e nevoie de un loc care alege familia dupa configurare
    (deci un switch tot apare, o singura data); pentru doua produse e disproportionat.
La acest laborator Simple Factory ajunge; Abstract Factory merita cand familiile cresc.


7. PRINCIPIILE SOLID IN PROIECT
-------------------------------
DIP  UI (ConsoleUI), service-urile si Main depind de abstractii: ConsoleUI primeste UtilizatorService,
     PrietenieService, ReteaService; *ServiceImpl primesc Repository<ID,E>; repository-urile primesc
     Validator<E>. Main lucreaza cu tipurile interfetelor (UI, UtilizatorService, Repository...).
OCP  Un tip nou de persistenta se adauga ca o clasa noua care implementeaza Repository (ex. DB) si un
     case in RepositoryFactory; service-ul si UI-ul nu se modifica. Validatori noi se adauga fara a atinge
     repository-ul.
SRP  Fiecare clasa are un rol: UtilizatorFileRepository doar converteaza linie<->entitate;
     AbstractFileRepository doar citeste/scrie fisierul; RepositoryFactory doar creeaza;
     LoadWarnings doar aduna mesaje; ConsoleUI doar interactioneaza cu utilizatorul;
     ReteaServiceImpl doar analizeaza graful.
LSP  InMemoryRepository, AbstractFileRepository (si subclasele) si *DbRepository respecta contractul din
     JavaDoc-ul Repository (null -> IllegalArgumentException; save intoarce null la succes). Un
     UtilizatorFileRepository poate inlocui un InMemoryRepository fara ca apelantul sa observe diferenta.
     Singura exceptie declarata: scheletele DB arunca UnsupportedOperationException pana la Lab 4.
ISP  Interfete mici si specifice: Validator (o metoda), UI (o metoda), Repository (CRUD), iar service-urile
     sunt trei interfete separate (utilizatori, prietenii, retea), nu una mare. UI-ul vede doar ce
     foloseste.


8. SABLOANE DE PROIECTARE
-------------------------
Simple Factory    RepositoryFactory (+ RepositoryType).
Template Method   AbstractFileRepository: scheletul (ensureLoaded, readLines, incarcaLinie, writeData, save/
                  delete/update) este fix; subclasele implementeaza doar extractEntity(String) si
                  createEntityAsString(E).
Strategy          Validator<T> cu implementari interschimbabile (UtilizatorValidator, PrietenieValidator),
                  injectate in repository. Tot Strategy, la nivel mai larg: Repository ca strategie de
                  persistenta (memorie / fisier / DB), aleasa din configurare.
Decorator         BufferedReader peste FileReader si BufferedWriter peste FileWriter (AbstractFileRepository):
                  adauga buffering si readLine()/newLine() peste fluxul de baza, cu aceeasi interfata Reader/Writer.
Dependency Injection  Main construieste obiectele si le paseaza prin constructor.


9. STRUCTURA PE STRATURI (ro.ubb.map.retea)
-------------------------------------------
  domain      Entity, Tuple, Utilizator, Prietenie (fara "final", cu getteri si setteri)
  validators  Validator<T>, UtilizatorValidator, PrietenieValidator
  exceptions  ValidationException, RepositoryException, ServiceException, EntityNotFoundException, ConfigException
  repository  Repository, LoadWarnings
              memory/InMemoryRepository
              file/AbstractFileRepository, UtilizatorFileRepository, PrietenieFileRepository
              db/UtilizatorDbRepository, PrietenieDbRepository (schelete)
              factory/RepositoryFactory, RepositoryType
  service     interfetele UtilizatorService, PrietenieService, ReteaService + *Impl, ComunitateSociabila
  ui          UI, ConsoleUI
  Main        incarca configurarea, construieste obiectele, porneste UI-ul
Dependentele merg intr-un singur sens: ui -> service -> repository -> domain.


10. ALGORITMI
-------------
Numar de comunitati (ReteaServiceImpl.comunitati / numarComunitati):
  Graful are utilizatorii ca noduri si prieteniile ca muchii (liste de adiacenta). Pentru fiecare nod
  nevizitat se porneste un DFS care il marcheaza, impreuna cu tot ce se poate ajunge din el; fiecare
  pornire inseamna o componenta conexa noua. Un utilizator fara prieteni formeaza singur o comunitate.

Cea mai sociabila comunitate (ReteaServiceImpl.ceaMaiSociabilaComunitate):
  Pentru fiecare componenta, din fiecare nod se porneste un backtracking: se extinde drumul cu un vecin
  care nu e deja pe drum, se coboara recursiv, apoi se scoate vecinul ca sa se incerce alte variante.
  Se retine cel mai lung drum elementar (fara noduri repetate), ca numar de muchii.
  Componenta cu drumul cel mai lung castiga; la egalitate ramane prima gasita.
  Complexitate exponentiala in cazul rau - acceptabil pentru retele mici.


11. DATE DE DEMONSTRATIE
------------------------
data/utilizatori.txt: 10 utilizatori. data/prietenii.txt: 3 comunitati - un lant 1-2-3-4-5 (drum de 4 muchii,
cea mai sociabila), o comunitate densa {6,7,8,9} (graf complet) si utilizatorul izolat 10.
data/test/: utilizatori_corupt.txt si prietenii_corupt.txt - cate o linie buna si cate un exemplu din fiecare
tip de linie corupta.
