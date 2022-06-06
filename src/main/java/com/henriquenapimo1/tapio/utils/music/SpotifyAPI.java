package com.henriquenapimo1.tapio.utils.music;

import com.henriquenapimo1.tapio.utils.Utils;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.util.*;

public class SpotifyAPI {

    private final SpotifyApi api;
    private final HashMap<String,List<QuizTrack>> quizPlaylists;

    public SpotifyAPI() {
        this.api = new SpotifyApi.Builder()
                .setClientId(Utils.spotifyID)
                .setClientSecret(Utils.spotifySecret)
                .build();

        ClientCredentialsRequest clientCredentialsRequest = api.clientCredentials().build();
        try {
            final ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            api.setAccessToken(clientCredentials.getAccessToken());
        }catch (Exception e) {
            //
        }

        this.quizPlaylists = new HashMap<>();
    }

    public void loadPlaylists() {
        Map<String,String> playlists = new HashMap<>();
        playlists.put("indie2016","37i9dQZF1DX0XnW5YpwS0o");
        // TODO: mais playlists

        playlists.forEach((name,id) -> quizPlaylists.put(name,getPreviewFromPlaylist(id)));
    }

    public List<QuizTrack> getPlaylistTracks(String name) {
        return quizPlaylists.get(name);
    }

    private List<QuizTrack> getPreviewFromPlaylist(String playlistID) {
        List<QuizTrack> previewMap = new ArrayList<>();

        try { // dá load na playlist do spotify
            Arrays.stream(api.getPlaylistsItems(playlistID).build().execute().getItems()).toList().forEach(t -> {
                try {
                    // pega cada track da playlist e carrega o url de preview dela
                    Track track = api.getTrack(t.getTrack().getId()).build().execute();
                    if(track != null && track.getPreviewUrl() != null)
                        previewMap.add(new QuizTrack(track.getName(),track.getArtists()[0].getName(),track.getPreviewUrl()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            System.out.println("Playlists carregadas com sucesso!");
            return previewMap;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
