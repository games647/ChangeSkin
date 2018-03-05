package com.github.games647.changeskin.core.model.skin;

import com.github.games647.changeskin.core.model.UUIDTypeAdapter;

import java.util.Map;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

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
        assertThat(skinTexture.getMetadata().getModel(), is("slim"));
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
        assertThat(skinTexture.getMetadata(), nullValue());
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
        SkinModel slimModel = new SkinModel(-1, 1517001205327L,
                UUIDTypeAdapter.parseId("78c3a4e837e448189df8f9ce61c5efcc"),
                "Rashomon_", true, "173567ea72ad4a22bf70bcba5fed3b8b9ea024639131bdd863c25d22f89",
                "",
                hexStringToByteArray("4E204F84DD36EFE474C0EDDDDBBB471DAFD1F7E4D1A52DEEB498507258F5E04182BAF5317A4D5" +
                        "A4E4E84C75E23B01B8CDEC21286F7CDB7F93C8252FAB2DE216C1512120BCCCA9AC0BBBFFB7195F9F466C48EF77" +
                        "681215ED5D6525602221BEB3FCABEA95C8711D727E392AEC95DC090E780A3FCA8E43B518A49E17C32389D80759" +
                        "31C97DFB0D1BAE00A94BC662451AE85B7CC0FA6BE5D9D48986959504DD2EB36F6AEA4BD3818BCDFC9C0F17FD0A" +
                        "6F0F4E3172E0790C31D1293CE4051F32F4592279E89DE71061B1AC20BEDA20511EE0A1246FB8481837141F7301" +
                        "C003D6119D99A617BC224F946D190BA7442E7EA43901D0B2AE9867C1EF35E94B9BB33FFED63EBB1C645AB32E0F" +
                        "4488AD4B4AF176DE80736FFA1058457CB8BA8789D394856DEF2339697BE8756097B13AA75A24DB534BF19E11B2" +
                        "98983F4129C24802251F228DC9F968E35E30DA0A47C0995255EDFAC0E68019805AC661EAC16E2A5A96E79609E9" +
                        "448CBC157EFC3708CB0AE4A0D9B7895B2D4043865D8433253B62F61A6D96687CCD4DF13D94AEE95CBDF5462F8C" +
                        "2C717A5F92BA2AE56D86908F85C68E4EB4B2CEB62AC576DF617870CA454FB6BD03AB619B97B34A14C1E073246F" +
                        "11C7550DFA2F34EBCA34D48CC23FC9EA98B316624D3D90678B109C7559373FFFECE755258C5CC2BF86E3D52F43" +
                        "E33B5D0EFABF1C779D44482ADC63B5EBF919A4736CEB9CB"));

        assertThat(VerifyUtil.isValid(slimModel.getEncodedValue(), slimModel.getSignature()), is(true));

        SkinModel steveModel = new SkinModel(-1, 1516999838117L,
                UUIDTypeAdapter.parseId("c883f59d66214243b64b3781d9fd9530"),
                "Malachiel", false, "e5edb4126b35ccab960276d1aecffb51f9fc1a4776eb9609a4fdbd9784e54",
                "",
                hexStringToByteArray("C1F320BF89677DBB8F68C12E71A5ED396EB57EC0530ED196A8533E7A6B6761F51257F41FE64EFF2" +
                        "EDF0FCC4893ABC15300D1ABAC258770D5BCA292B562607C7D26AEEE7AC49BF59A50FC2CE247A53D9E53154D34C80" +
                        "920CDCAC458E31C37A4D42A2BDD34674DEC4C3D04ECF0E1960D6172B6F0CDC8BB5D4B956C77071E7276CDBC28469" +
                        "A38584F14283CD624B782E75B51FD87AEE02A62127ECB3147F6383AD3ED165239C503778FCEE41C1E648C4AC7D9A" +
                        "DEEB2ACF18C78722E4C1A331ECC373C2D84A3E5473D4A8D8818C22C54F7601A0713D9EC7B70C0E7709DED19DFAA5" +
                        "CF3F832A8AF1B4B522BBEB64F9808120FAD39AC53095075818B896BD8405660866F752B850714E6441A8162349B3" +
                        "1F5469F84DF001EF071C190145820405B0457FD1A8CB8455190D87AD1870CDEFF53B66CFD09192CC8D911B69E3EF" +
                        "F06F8A5554FE5E7AF00B2E87FBEBFE47F2FC65CF4EBB675F5C1285B8D149B194B83C19A34CE718F56858890A1CDB" +
                        "52CF8E3316111A8637C5801FAD46BAFDF9B190DF4DE4B0CB3B80D398374A0B106519FE4ABCC6919AC656FDD4B669" +
                        "5E8D0D291C3D5D6448DAA48F9CE18AA770217BF938DFE47A1FF310D206AB0EA0C77F1D3C5922366DAA6D53646907" +
                        "AE339C95EEE21B41BF078D89FF45AC82E8BD52085367D6CF046D40D5ACA0A029991D7DC917C46D3AA6413020135B" +
                        "F73B3D23B01030971EE00BFD6"));
        assertThat(VerifyUtil.isValid(steveModel.getEncodedValue(), steveModel.getSignature()), is(true));
    }

    private static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }

        return data;
    }
}
