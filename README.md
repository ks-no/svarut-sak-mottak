Importmodul for SvarUt
===================================

Sakimport-modulen er skrevet i Java og støtter direkte import av forsendelser fra SvarUt til Ephorte. Sakimport bruker
Geointegrasjonsstandard versjon 1.1. Sakimport henter forsendelser via webservicene til SvarUt. All kommunikasjon
foregår kryptert over TLS. Nyere aktivitet fra sakimport logges til svarutSakimport.log-filen, og eldre logger finnes i
logg-katalogen (genereres når loggene roteres).

Nyeste versjon av sakimport kan lastes ned via SvarUt, en må ha tilgang til SvarUt for å få lov å laste ned filen.
[Svarut-sak-import|https://svarut.ks.no/releases/svarut-sak-import-latest.zip]

Hvordan bruke Sakimport
-----------------------------------
Sakimport distribueres som en zipfil. Denne inneholder denne readme-filen, applikasjonen (svarut-sak-import.jar), et
bat-script som starter applikasjonen, samt en katalog med konfigurasjonsfiler. Før du kan begynne å bruke applikasjonen
må config.properties oppdateres. Følgende felter må fylles ut:

1. svarutbrukernavn -- Brukernavn for SvarUt
2. svarutpassord -- Passord for SvarUt
3. svaruturl -- URL til SvarUt sin webservice for mottaksmodul
4. sakbrukernavn -- Brukernavn for sakssystem
5. sakpassord -- Passord for sakssystem
6. sakurl -- URL til sakssystemets webservice
7. saksnr -- Default saksnummer for saker som ikke allerede eksisterer i sakssystemet
8. saksaar -- Hvilket saksår forsendelsen legges inn med
9. hostname -- hostname for serveren som sakImport kjører på
9. privatekeyfil -- Sti og navn til privat nøkkel for å dekryptere nedlastede forsendelser

Egen konfigurasjonsfil kan legges i konfigurasjonskatalogen og kun filnavnet angis som kommandolinjeparameter:

`java -jar svarut-sak-import.jar -konfigurasjonsfil <filnavn>`

Alle parametre kan også gis som kommandolinjeparametre. For å sette default saksår:

`java -jar svarut-sak-import.jar -saksaar 2015`

Kommandolinjeparametre har høyere prioritet enn verdier for samme parameter i konfigurasjonsfilen. Sakimport leter
først etter egendefinert konfigurasjonsfil angitt som beskrevet over. Dersom denne ikke er angitt brukes
config.properties. Hvis kommandolinjeparametre er satt ved oppstart vil disse bli brukt fremfor det som står i
konfigurasjonsfilen. Det er ikke obligatorisk med alle felter i konfigurasjonsfilen, men dersom de ikke finnes der må
de oppgis som kommandolinjeparametre. Redigér konfigurasjonsfilen i forhold til hva som passer best for deres system.

Alle forsendelser som lastes ned med Sakimport er kryptert. For at dette skal fungere må det offentlige sertifikatet
deres lastes opp i SvarUt (samme sted som servicepassord genereres) med nivå 4-innlogging. Sakimport kan ikke tas i bruk
uten dette sertifikatet. I tillegg må det private sertifikatet (X509-sertifikat, .pem-filer) være tilgjengelig for
Sakimport, slik at forsendelsene kan dekrypteres. For å generere RSA nøkkelpar med openSSL kan følgende kommando brukes:

`openssl req -x509 -newkey rsa:2048 -nodes -keyout key.pem -out cert.pem -days 999`

Dette sertifikatparet vil være gyldig i 999 dager og er ikke låst med passord. Last opp cert.pem til SvarUt og gjør
key.pem tilgjengelig for importmodulen.

Forsendelser er tilgjengelig for sakimport i 2 timer og sakimport bør derfor settes opp som en gjentagende jobb
(cron-jobb eller en scheduled task). Forsendelser som ikke hentes innen tidsfristen blir sendt til Altinn og følger det
normale løpet for forsendelser i SvarUt. I et Windows-basert system må task scheduler være aktivert. Denne kan
konfigureres via kommandolinjen eller gjennom det grafiske brukergrensesnittet. For å sette opp en jobb som kjører
hvert 15. minutt kan følgende kommando brukes:

`schtasks /create /sc minute /mo 15 /ru SYSTEM /tn "SvarUt Sakimport" /tr \Sti\til\runSakimport.bat`

Her er systembruker satt opp fordi jobben da vil kjøres uavhengig av tidspunkt. Dersom annen bruker benyttes må denne
være pålogget konstant. For å slette den periodiske jobben:

`schtasks /delete /tn "SvarUt Sakimport"`

Nedlasting av siste versjon
-----------------------------------

[Siste versjon av SvarUt Sakimport](https://svarut.ks.no/releases/svarut-sak-import-latest.zip)

[Eldre versjoner](https://svarut.ks.no/releases/)

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
