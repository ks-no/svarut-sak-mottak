Importmodul for SvarUt
===================================

Sakimport-modulen er skrevet i Java og støtter direkte import av forsendelser fra SvarUt til Ephorte. Sakimport bruker
Geointegrasjonsstandard versjon 1.1. Sakimport henter forsendelser via webservicene til SvarUt. All kommunikasjon
foregår kryptert over TLS. Nyere aktivitet fra sakimport logges til svarutSakimport.log-filen, og eldre logger finnes i
logg-katalogen (genereres når loggene roteres).

Nyeste versjon av sakimport kan lastes ned via SvarUt, en må ha tilgang til SvarUt for å få lov å laste ned filen.
[Svarut-sak-import](https://svarut.ks.no/releases/svarut-sak-import-latest.zip)

Hvordan installere Sakimport
-----------------------------------

Forbredelser:

Sakimport må kjøre på en server som har tilgang til https://svarut.ks.no og saksystemet sin geointegrasjon webservice.

1. Når server er valgt må java 8 installeres med unlimited strength cryptography.
[Java8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[Java 8 unlimited strength crypto](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)

2. For ephorte brukere, må det legges inn ny config/lisens fil, kontakt every for å få denne.

2. Ephorte må ha to merknadstyper SVARUT-MOT og SVARUT-MET, disse heter Informasjonstype i Ephorte.

3. Sakimport må ha en bruker som kan opprette journalposter i sakssystemet. Det må også opprettes en fordelings sak, hvor forsendelser som ikke
kan puttes direkte på en sak havner.

4. Kontakt svarut@ks.no for å få opprettet mottakersystem i SvarUt. Vi trenger fødselsnr og navn til person som skal konfigurere mottakersystemet.

5. Alle forsendelser som lastes ned med Sakimport er kryptert. For at dette skal fungere må det offentlige sertifikatet
deres lastes opp i SvarUt (samme sted som servicepassord genereres) med nivå 4-innlogging. Sakimport kan ikke tas i bruk
uten dette sertifikatet. I tillegg må det private sertifikatet (X509-sertifikat, .pem-filer) være tilgjengelig for
Sakimport, slik at forsendelsene kan dekrypteres. For å generere RSA nøkkelpar med openSSL kan følgende kommando brukes:
`openssl req -x509 -newkey rsa:2048 -nodes -keyout privatekey.pem -out public.pem -days 99999`
Dette sertifikatparet vil være gyldig i 99999 dager og er ikke låst med passord. Last opp public.pem til SvarUt og gjør
privatekey.pem tilgjengelig for importmodulen.
For windows kan en laste ned: https://slproweb.com/products/Win32OpenSSL.html
`openssl req -x509 -newkey rsa:2048 -nodes -keyout privatekey.pem -out public.pem -days 99999 -config c:\<opensslinstallmappe>\bin\openssl.cfg`
public.pem lastet opp i svarut på mottakersystem.
privatekey.pem er hemmelig og skal brukes av sakimport.

6. Sakimport må ha urlene til geointegrasjon webservicen, vi trenger url for SakArkivOppdateringService og SakArkivInnsynService

Installasjon:

Sakimport distribueres som en zipfil. Denne inneholder denne readme-filen, applikasjonen (svarut-sak-import.jar), et
bat-script som starter applikasjonen, samt en katalog med konfigurasjonsfiler. Før du kan begynne å bruke applikasjonen
må config.properties oppdateres. Følgende felter må fylles ut:

1. svarutbrukernavn -- Brukernavn for SvarUt (finnes under mottakersystem i svarut)
2. svarutpassord -- Passord for SvarUt (finnes under mottakersystem i svarut)
3. svaruturl -- URL til SvarUt sin webservice for mottaksmodul (https://svarut.ks.no/tjenester/svarinn/mottaker/hentNyeForsendelser for produksjon)
4. sakbrukernavn -- Brukernavn for sakssystem
5. sakpassord -- Passord for sakssystem
6. sakurl -- URL til geointegrasjon SakArkivOppdateringService service
4. sakinnsynurl -- URL til geointegrasjon SakArkivInnsynService service
7. saksnr -- Saksnummer til en fordelingssak
8. saksaar -- Saksår for fordelingssak.
9. hostname -- hostname for serveren som sakImport kjører på, dette må være hostname som GeoIntegrasjon får tilgang til.
9. privatekeyfil -- Sti og navn til privat nøkkel for å dekryptere nedlastede forsendelser

Egen konfigurasjonsfil kan legges der du pakket opp zip fila og vil da kunne finnes automatisk av programmet eller du kan angi konfigurasjonsfila som kommandolinjeparameter:

`java -jar svarut-sak-import.jar` laster da config.properties i samme mappe
`java -jar svarut-sak-import.jar -konfigurasjonsfil <filnavn>`

Alle parametre kan også gis som kommandolinjeparametre. For å sette default saksår:

`java -jar svarut-sak-import.jar -saksaar 2015`

Kommandolinjeparametre har høyere prioritet enn verdier for samme parameter i konfigurasjonsfilen. Sakimport leter
først etter egendefinert konfigurasjonsfil angitt som beskrevet over. Dersom denne ikke er angitt brukes
config.properties. Hvis kommandolinjeparametre er satt ved oppstart vil disse bli brukt fremfor det som står i
konfigurasjonsfilen. Det er ikke obligatorisk med alle felter i konfigurasjonsfilen, men dersom de ikke finnes der må
de oppgis som kommandolinjeparametre. Redigér konfigurasjonsfilen i forhold til hva som passer best for deres system.

Forsendelser er tilgjengelig for sakimport i 24 timer og sakimport bør derfor settes opp som en gjentagende jobb
(cron-jobb eller en scheduled task). Forsendelser som ikke hentes innen tidsfristen blir sendt til Altinn og følger det
normale løpet for forsendelser i SvarUt. I et Windows-basert system må task scheduler være aktivert. Denne kan
konfigureres via kommandolinjen eller gjennom det grafiske brukergrensesnittet. For å sette opp en jobb som kjører
hvert 15. minutt kan følgende kommando brukes:

`schtasks /create /sc minute /mo 15 /ru SYSTEM /tn "SvarUt Sakimport" /tr \Sti\til\runSakimport.bat`

Her er systembruker satt opp fordi jobben da vil kjøres uavhengig av tidspunkt. Dersom annen bruker benyttes må denne
være pålogget konstant. Hvis scheduled task ikke fungerer kan det være lurt å endre action, og start in til mappa svarut sak import ligger
.

For å slette den periodiske jobben:

`schtasks /delete /tn "SvarUt Sakimport"`

For å overvåke applikasjonen har vi laget noen loggfiler:
`logg/feilkjoringer.log` får en ny linje hver gang importen feiler på en eller flere forsendelser.
`logg/forsendelser.log` får en linje for hver importerte forsendelse.

Noen kommuner har satt opp overvåking på når disse filene sist ble endret. Feilkjoringer.log skal ikke være endret siste 2 timer f.eks
Forsendelser.log skal være endret for hver scheduled task, f.eks for ikke mindre enn 20 min siden.

Nedlasting av siste versjon
-----------------------------------

[Siste versjon av SvarUt Sakimport](https://svarut.ks.no/releases/svarut-sak-import-latest.zip)

[Eldre versjoner](https://svarut.ks.no/releases/)

Versjoner:

<ul>
    <li>11. Første release</li>
    <li>12. Støtte for å kunne svare på forsendelse og putte forsendelse på korrekt sak. (for Ephorte krever dette versjon 1.0.4.2 av ephorte5GI og 5.3.0 av ncore)
    </li>
    <li>13. Støtte for å spesifisere Journalstatus på importerte journalposter (M/J), Støtte for å endre klientnavn som sendes ved kall til GI. Støtte for å endre port for nedlasting av dokumenter.
    </li>
    <li>14. Fikset dokumentetsdato og andre datoer i metadata merknaden.
        </li>
</ul>

Oppgraderings rutine:
* Last ned zip fil
* Erstatt eksisterende filer med de i zip filen.





Utvikling av sakimport
===================================

Denne delen er kun relevant dersom du skal sette opp utviklingsmiljøet til sakimport.


Bygge og deploye sakimport
-----------------------------------
1. Sjekk ut mottaksmodulen fra git `git clone https://github.com/ks-no/svarut-sak-mottak.git`
2. Gå inn i prosjektet og bygg sak-import `mvn clean install` Dette setter opp fakeServices, som så startes når testene
for sak-import-prosjektet kjører.
3. For å lage distribusjonspakke av sakimport, gå inn i svarut-sak-import-prosjektet. Etter å ha kjørt clean install,
bygg zip-fil slik: `mvn assembly:single`
I zip-filen ligger en katalog som inneholder jar som skal kjøres, readme, en katalog med konfigurasjonsfiler, samt et
bat-script for å kjøre applikasjonen.
