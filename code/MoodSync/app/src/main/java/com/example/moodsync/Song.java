package com.example.moodsync;

/**
 * A model class representing a song. This class contains details about a song,
 * including its ID, title, singer, and URL. It provides getter and setter methods
 * for accessing and modifying these properties.
 *
 * <p>
 * This class is designed to work with Firebase Firestore, which requires an empty
 * constructor for deserialization.
 * </p>
 */
public class Song {
    private Long id;
    private String singer;
    private String title;
    private String url;

    /**
     * Default constructor required by Firebase Firestore for deserialization.
     */
    public Song() {
    }


    /**
     * Constructs a new {@code Song} object with the specified details.
     *
     * @param id     The unique identifier of the song.
     * @param singer The name of the singer or artist of the song.
     * @param title  The title of the song.
     * @param url    The URL of the song (e.g., a link to an audio file or streaming service).
     */
    public Song(Long id, String singer, String title, String url) {
        this.id = id;
        this.singer = singer;
        this.title = title;
        this.url = url;
    }

    /**
     * Returns the unique identifier of the song.
     *
     * @return The ID of the song.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the name of the singer or artist of the song.
     *
     * @return The singer's name.
     */
    public String getSinger() {
        return singer;
    }

    /**
     * Returns the title of the song.
     *
     * @return The title of the song.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the URL of the song (e.g., a link to an audio file or streaming service).
     *
     * @return The URL of the song.
     */

    public String getUrl() {
        return url;
    }

    /**
     * Sets the unique identifier of the song.
     *
     * @param id The ID to set for this song.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Sets the name of the singer or artist of the song.
     *
     * @param singer The singer's name to set for this song.
     */
    public void setSinger(String singer) {
        this.singer = singer;
    }

    /**
     * Sets the title of the song.
     *
     * @param title The title to set for this song.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the URL of the song (e.g., a link to an audio file or streaming service).
     *
     * @param url The URL to set for this song.
     */
    public void setUrl(String url) {
        this.url = url;
    }
}