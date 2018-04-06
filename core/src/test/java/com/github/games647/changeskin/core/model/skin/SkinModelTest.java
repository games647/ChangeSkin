package com.github.games647.changeskin.core.model.skin;

import com.github.games647.changeskin.core.model.UUIDTypeAdapter;

import java.util.Base64;
import java.util.Map;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import static org.junit.Assert.assertThat;

public class SkinModelTest {

    private static final String SLIM_VALUE = "eyJ0aW1lc3RhbXAiOjE1MTcwNTUyNjIxODgsInByb2ZpbGVJZCI6Ijc4YzNhNGU4MzdlNDQ" +
            "4MTg5ZGY4ZjljZTYxYzVlZmNjIiwicHJvZmlsZU5hbWUiOiJSYXNob21vbl8iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHV" +
            "yZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE3MzU2N2VhNzJhZDRhMjJiZjc" +
            "wYmNiYTVmZWQzYjhiOWVhMDI0NjM5MTMxYmRkODYzYzI1ZDIyZjg5IiwibWV0YWRhdGEiOnsibW9kZWwiOiJzbGltIn19fX0=";
    private static final String SLIM_SIGNATURE = "X3pND0qkG0IEPBIgeYem7OmghO0LPV3J39LKWd3HinZQeJjGcmRbkvFXBaeYV0Vl8CZ" +
            "ub8/dHQbgUtPnnWL5J0x8KLRFVp9p3uuWxbGbx8efN6SAE98uC7xbphTLxBtNNKbCTOwNDhI8WfRdf3LaKLNhdPi1qSMzQvTaV9q8eYK" +
            "wdnwJ7DPCQrcPHvlZwnxLH6iIn3nPXuMzTu7aWRKF2IwGZ72Pa3X1RWy4QHtOPuZY6DKJxQK1hkbD1YNhjWnQ/8o/OJaiTmlZM0rWrjM" +
            "YCZdYcCpeeFV+gsRHuhBG9LHz7hePJvysAo005py3ydr+3PUi3ISsVYFJ9ygJqIgbqKjov8+zVfnAZHusQMdBaoDlH05sae5gAGai5zM" +
            "Ta7UwObMfsqlHTNA+Ch9kAJQ2WmYqJZeAZgrGw8MkF23zMRKMZKqLtwCdiwJfiAgtJy148+HbtZuyi3obNcS+hn9gxn5LaGC+NUOCxXH" +
            "DbCF4xN+on1/kLgyje7TjTUQnMAs5CWRWulVnt3aOOon0mUk2xMkv6B6WwW1n0MAU2jbhhp3s/cEQFrdr1f5IMLQ/OEXo2u5PPbHzyUI" +
            "So0JhuZQLNNUG2ZuLpi+eo3DccfOM/HllaBPOuA5rHU46slTgxI4edTKsG+C2vbUSFo1+vq4TFyEkoY2G0I6aRWVpDQosAxw=";

    private static final String OLD_SLIM_VALUE = "eyJ0aW1lc3RhbXAiOjE1MDQ2Mjk1ODUxMjEsInByb2ZpbGVJZCI6IjNkOWNmOTZiN2M" +
            "yNzRiZWVhZDFiOWQ0NTM3NTRjYjc2IiwicHJvZmlsZU5hbWUiOiJOaWtha2EiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHV" +
            "yZXMiOnsiU0tJTiI6eyJtZXRhZGF0YSI6eyJtb2RlbCI6InNsaW0ifSwidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQ" +
            "vdGV4dHVyZS83ZjY5Mjk5MmIxOGJmMmY3MTVlMDc3NTNkYmM3YmJlZjU0M2FhYTI2ZThlM2YxYWY0NGI5ZjU0Njg5ODM0ZWEifX19";
    private static final String OLD_SLIM_SIGNATURE = "FqiJXjKiEp3J9tK/VL/AwmB9ZHSvbOy9J9If1SYkiJM1WvhINm3kAeLOCeL/Tr8" +
            "FvwuP0uHiLbn5P0b5qglIjxIt9RIZeJT/PazsXHuVQO0yTub1+mftu4EwtTxKj0fX1PV7vsXhlL43nZ/ucQFyRISYctbvHTaUayGmiLu" +
            "tPkHnSqKOv3iM06svzS4UvjOnP8/llflVgt7cYjln1Tn0Y2sSwHpVedcLcBroDIXBM1z369gavI6i9VG+BbLPm+wj7+bi78jbBxrUmxv" +
            "svD9uBEdwZub6W1ZGGk4ev0D7jucoC494PNOSpojOdOKgH1ctHwxkjONiTmBmq0g5I1I6JQSS5F9+K/kPtMh0vtT3ff9AZxjAQ+dNyYr" +
            "IQWh4ma19pNVyKQNtXjacowoXmAPeKzajklc6/u8ZFAwiJzPZsIKhVrUZBUXPQ/mJzlbryORqid05rjWOnhHqIDGtyEz2+aof84uk20E" +
            "nENDznmtq+GsuzEo6IPDf/H20Y/+9WIOzgY32LK0s6wGQschgyNHiFIyMbmzLnHwgoOXm67s6fKWa9ovgTzcXndsO5hDjjL3En2NfPAD" +
            "kXZP1u201FW8Px6qZUhSXOxmJSqPDYSlEaiZw5yx2Vtq/ylSoZsG8iFWezIg9cxF4Z1lLkPeQMioFX9EvL0anlitc/TUFtOFoiHA=";

    private static final String STEVE_VALUE = "eyJ0aW1lc3RhbXAiOjE1MTcwNTI0MzU2NjgsInByb2ZpbGVJZCI6IjBhYWEyYzEzOTIyYT" +
            "QxMWJiNjU1OWI4YzA4NDA0Njk1IiwicHJvZmlsZU5hbWUiOiJnYW1lczY0NyIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dX" +
            "JlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTJlNmEzZjhjYWVhNzkxM2FiND" +
            "gyMzdiZWVhNmQ2YTFhNmY3NjkzNmUzYjcxYWY0YzdhMDhiYjYxYzc4NzAifX19";
    private static final String STEVE_SIGNATURE = "JTfVSrd+glDHO2glPmsKdeTOXPgHw6mBnvFbZA75TBq7sQoIMoWjxOlvH7vPkPyTkM" +
            "sb5Vt4E6jsU73hi1FDYUaoGvzmTHzhN1scXluagx1jsye6jbAx64HK+0Iw5/8nwQUTVUP6ttxLC+2HZvIeNoYc6Dqd7HAIwcdxHFjDVb" +
            "MXAfMT33C0N1CTlvnEwbbK+Fx155Fg1nKU/PYoaSXWL9eEMwCLlpf/UTTegmDlpOwlo9zG2f8/YkhACE8gyJZOB+WJwf1+Vv3BUTuAnM" +
            "AKy7KztZDZE1119fBfVLblGykniAO63BATWTWqP/oTQFCSkmpPGMyznaAPJRt4/IfES4uxYAfXCxKWF4ZytdenAmbRo00ZVg77l6wdst" +
            "xsdGaZtYEB5nsdF6lehRWLWVYhUX5nHk2HCfkGboXjhmFgcCLzFcV+YSC//P0CN2GDBlVGUPybTxceRjg7UoA4O9mn+1bLvTD7C8/G8k" +
            "RpqLRNK9/Wm8cf2sMbNCP6gPSlGao1nIuZsg7+eRih1G1LilJwtOaFhFeH+Pu+CUMCIZPxLtjTwZopG8P0FAwCTpO0gJJrqyMT+pozGA" +
            "fJ3mbt4uzuq5Mg1XYjazqEz5Zg8n0JwdTP0ZkoiVy4VMDeQz+C31bUmPcSDLxpJYF3uKQCGlbL1UZshcnQHXEEUhwb3bqjPKA=";

    public static final String CAPE_VALUE = "eyJ0aW1lc3RhbXAiOjE1MjAyNzc1NzIzMjIsInByb2ZpbGVJZCI6IjYxNjk5YjJlZDMyNzRh" +
            "MDE5ZjFlMGVhOGMzZjA2YmM2IiwicHJvZmlsZU5hbWUiOiJEaW5uZXJib25lIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1" +
            "cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jZDZiZTkxNWIyNjE2NDNmZDEz" +
            "NjIxZWU0ZTk5YzllNTQxYTU1MWQ4MDI3MjY4N2EzYjU2MTgzYjk4MWZiOWEifSwiQ0FQRSI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMu" +
            "bWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2VlYzNjYWJmYWVlZDVkYWZlNjFjNjU0NjI5N2U4NTNhNTQ3YzM5ZWMyMzhkN2M0NGJmNGViNGE0" +
            "OWRjMWYyYzAifX19";
    public static final String CAPE_SIGNATURE = "qiURFe/44jgLijrsWAyKbhbQcCJ6Cv0yj1qWpuoY7dH+OA5RIoipxxg5rmAAUSD9SUxB" +
            "ieVdsX1poc4U4OBfnkiwBKz/yIhpT9QQgN0mBOIX7seNf9/Acnvw75neGqn7LKpvHz8Kxn5zwsd8MGzXmpZR4n2Clwb+IjO7TX13v03E" +
            "GxbjMJzRKTGMzLCeQDaXk1G13uYy6q6yrf8fPK2FAaeHbMnr1Csj11up+seTAy19A3fK27P05OT6nHlkUNhF5e/h4Qtjd/V98/JfAUTw" +
            "mLca5gu2VRERXvDUm6rnP4paYawhH0YXQwVSCOujqjI2p4V/mnbTTILz4MFKRwwzwnmfcr6/LgC7kDpe6H3VvK3x/Y5chKl8aUNUC8b+" +
            "cX0cDKE8dRSm0BirQjEmA6W76+AF6A7pSWufc7FnhyFROxpc/Z+qfBSwgT85BWqc7LmAgjoZLD+kZzF/hvsJlFrfaTZNZ5z7I26h0mke" +
            "E2fwVoNmiHIDOPAgCkehv/HfEg5r45Q30MkxXFzsV9UEkFINo3GZOq8p3NTjhlj7OGjUQWpMiKu62+LWohGIm6NbKq2aYM/cPs0jSZ3s" +
            "9lbofsHsEl301JyyI0OCbR4ltpgt6SpXOGInkVsbqbrRenL2gBun2fga0NNezgQHs0rZHXCnVGLuVm2kUJvSF9RgxwhXhKY=";

    @Test
    public void testParsingSlim() throws Exception {
        SkinModel skin = SkinModel.createSkinFromEncoded(SLIM_VALUE, SLIM_SIGNATURE);

        assertThat(skin.getTimestamp(), is(1517055262188L));
        assertThat(skin.getProfileId(), is(UUIDTypeAdapter.parseId("78c3a4e837e448189df8f9ce61c5efcc")));
        assertThat(skin.getProfileName(), is("Rashomon_"));

        assertThat(skin.getSaveLock(), notNullValue());

        Map<TextureType, TextureModel> textures = skin.getTextures();
        TextureModel skinTexture = textures.get(TextureType.SKIN);
        assertThat(skinTexture.getUrl(), is("http://textures.minecraft.net/texture/173567ea72ad4a22bf70bcba5fed3" +
                "b8b9ea024639131bdd863c25d22f89"));
        assertThat(skinTexture.isSlim(), is(true));
    }

    @Test
    public void testParsingSteve() throws Exception {
        SkinModel skin = SkinModel.createSkinFromEncoded(STEVE_VALUE, STEVE_SIGNATURE);

        assertThat(skin.getTimestamp(), is(1517052435668L));
        assertThat(skin.getProfileId(), is(UUIDTypeAdapter.parseId("0aaa2c13922a411bb6559b8c08404695")));
        assertThat(skin.getProfileName(), is("games647"));

        assertThat(skin.getSaveLock(), notNullValue());

        Map<TextureType, TextureModel> textures = skin.getTextures();
        TextureModel skinTexture = textures.get(TextureType.SKIN);
        assertThat(skinTexture.getUrl(), is("http://textures.minecraft.net/texture/a2e6a3f8caea7913ab48237beea6d" +
                "6a1a6f76936e3b71af4c7a08bb61c7870"));
        assertThat(skinTexture.isSlim(), is(false));
    }

    @Test
    public void testSignatureFromEncoded() throws Exception {
        SkinModel slimModel = SkinModel.createSkinFromEncoded(SLIM_VALUE, SLIM_SIGNATURE);
        assertThat(VerifyUtil.isValid(slimModel.getEncodedValue(), slimModel.getSignature()), is(true));

        SkinModel steveModel = SkinModel.createSkinFromEncoded(STEVE_VALUE, STEVE_SIGNATURE);
        assertThat(VerifyUtil.isValid(steveModel.getEncodedValue(), steveModel.getSignature()), is(true));
    }

    @Test
    public void testSignatureFromSerialized() throws Exception {
        SkinModel oldSlimModel = new SkinModel(-1, 1504629585121L,
                UUIDTypeAdapter.parseId("3d9cf96b7c274beead1b9d453754cb76"),
                "Nikaka", true, "7f692992b18bf2f715e07753dbc7bbef543aaa26e8e3f1af44b9f54689834ea",
                "", Base64.getDecoder().decode(OLD_SLIM_SIGNATURE));
        assertThat(VerifyUtil.isValid(oldSlimModel.getEncodedValue(), oldSlimModel.getSignature()), is(true));

        SkinModel slimModel = new SkinModel(-1, 1517055262188L,
                UUIDTypeAdapter.parseId("78c3a4e837e448189df8f9ce61c5efcc"),
                "Rashomon_", true, "173567ea72ad4a22bf70bcba5fed3b8b9ea024639131bdd863c25d22f89",
                "", Base64.getDecoder().decode(SLIM_SIGNATURE));
        assertThat(VerifyUtil.isValid(slimModel.getEncodedValue(), slimModel.getSignature()), is(true));

        SkinModel steveModel = new SkinModel(-1, 1517052435668L,
                UUIDTypeAdapter.parseId("0aaa2c13922a411bb6559b8c08404695"),
                "games647", false, "a2e6a3f8caea7913ab48237beea6d6a1a6f76936e3b71af4c7a08bb61c7870",
                "", Base64.getDecoder().decode(STEVE_SIGNATURE));
        assertThat(VerifyUtil.isValid(steveModel.getEncodedValue(), steveModel.getSignature()), is(true));

        SkinModel capeModel = new SkinModel(-1, 1520277572322L,
                UUIDTypeAdapter.parseId("61699b2ed3274a019f1e0ea8c3f06bc6"),
                "Dinnerbone", false, "cd6be915b261643fd13621ee4e99c9e541a551d80272687a3b56183b981fb9a",
                "eec3cabfaeed5dafe61c6546297e853a547c39ec238d7c44bf4eb4a49dc1f2c0", Base64.getDecoder().decode(CAPE_SIGNATURE));

        assertThat(VerifyUtil.isValid(capeModel.getEncodedValue(), capeModel.getSignature()), is(true));
    }
}
