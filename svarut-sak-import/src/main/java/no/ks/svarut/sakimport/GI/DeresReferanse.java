package no.ks.svarut.sakimport.GI;

import no.ks.svarut.sakimport.Mottaker;
import no.ks.svarut.sakimport.NoarkMetadataFraAvleverendeSakssystem;

public class DeresReferanse {
    private final Mottaker svarSendesTil;
    private final NoarkMetadataFraAvleverendeSakssystem noarkMetadataFraAvleverendeSystem;

    public DeresReferanse(Mottaker svarSendesTil, NoarkMetadataFraAvleverendeSakssystem noarkMetadataFraAvleverendeSystem) {
        this.svarSendesTil = svarSendesTil;
        this.noarkMetadataFraAvleverendeSystem = noarkMetadataFraAvleverendeSystem;
    }

    public Mottaker getSvarSendesTil() {
        return svarSendesTil;
    }

    public NoarkMetadataFraAvleverendeSakssystem getNoarkMetadataFraAvleverendeSystem() {
        return noarkMetadataFraAvleverendeSystem;
    }
}
