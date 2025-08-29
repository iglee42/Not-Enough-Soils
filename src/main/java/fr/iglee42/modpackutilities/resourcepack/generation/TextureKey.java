package fr.iglee42.modpackutilities.resourcepack.generation;

public record TextureKey(String key,String object) {

    public String toJson(){
        return "        \""+key+"\": \""+object+"\"";
    }
}
