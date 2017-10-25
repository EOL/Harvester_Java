package org.bibalex.eol.parser.formats;

import org.bibalex.eol.parser.models.Taxon;
import java.util.ArrayList;

/**
 * Created by Amr Morad
 */
public interface Format {

    void handleLines(ArrayList<Taxon> nodes);
}
