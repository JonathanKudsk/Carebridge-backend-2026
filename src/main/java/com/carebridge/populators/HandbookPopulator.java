package com.carebridge.populators;

import com.carebridge.dao.impl.HandbookDAO;
import com.carebridge.entities.Handbook;
import com.carebridge.entities.HandbookTab;

public class HandbookPopulator {

    public static void populate() {

        HandbookDAO handbookDAO = HandbookDAO.getInstance();

        Handbook handbook = new Handbook("Institutionshåndbog");

        HandbookTab rulesTab = new HandbookTab("Regler", 0);
        rulesTab.setContent("""
                <h1>Institutionsregler</h1>

                <p>Alle medarbejdere forventes at følge institutionens retningslinjer og procedurer.</p>

                <h2>Generelle regler</h2>

                <ul>
                    <li>Mød senest 10 minutter før vagtstart</li>
                    <li>Mobiltelefon må ikke anvendes i arbejdstiden uden fagligt formål</li>
                    <li>Alle hændelser skal dokumenteres i journalsystemet</li>
                    <li>Tavshedspligt gælder både under og efter ansættelse</li>
                </ul>

                <h2>Arbejdsmiljø</h2>

                <p>Vi følger Arbejdstilsynets anbefalinger omkring arbejdsmiljø og trivsel:</p>

                <p>
                    <a href="https://at.dk" target="_blank">
                        Arbejdstilsynet
                    </a>
                </p>
                """);

        HandbookTab emergencyTab = new HandbookTab("Beredskaber", 1);
        emergencyTab.setContent("""
                <h1>Beredskabsprocedurer</h1>

                <h2>Brand</h2>

                <ol>
                    <li>Evakuer bygningen roligt</li>
                    <li>Kontroller alle beboerværelser</li>
                    <li>Ring 112</li>
                    <li>Informér nærmeste leder</li>
                </ol>

                <h2>Vold eller trusler</h2>

                <ul>
                    <li>Skab afstand og tilkald hjælp</li>
                    <li>Følg institutionens konfliktnedtrapningsprocedure</li>
                    <li>Registrér hændelsen efterfølgende</li>
                </ul>

                <p>
                    Se også:
                    <a href="https://www.beredskabsinfo.dk" target="_blank">
                        BeredskabsInfo
                    </a>
                </p>
                """);

        HandbookTab linksTab = new HandbookTab("Nyttige links", 2);
        linksTab.setContent("""
                <h1>Nyttige links</h1>

                <ul>
                    <li>
                        <a href="https://www.retsinformation.dk" target="_blank">
                            Retsinformation
                        </a>
                    </li>

                    <li>
                        <a href="https://www.social.dk" target="_blank">
                            Social- og Boligstyrelsen
                        </a>
                    </li>

                    <li>
                        <a href="https://at.dk" target="_blank">
                            Arbejdstilsynet
                        </a>
                    </li>

                    <li>
                        <a href="https://www.sst.dk" target="_blank">
                            Sundhedsstyrelsen
                        </a>
                    </li>
                </ul>

                <p>
                    Disse links anvendes ofte i forbindelse med dokumentation,
                    arbejdsmiljø og institutionsdrift.
                </p>
                """);

        handbook.addHandbookTab(rulesTab);
        handbook.addHandbookTab(emergencyTab);
        handbook.addHandbookTab(linksTab);

        handbookDAO.create(handbook);

        System.out.println("Handbook populated");
    }
}