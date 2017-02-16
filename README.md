# Popular Movies
Part of the Associate Android Developers Fast Track Course

Welcome to Popular Movies
-------------------------




All movie data comes from themovieDB. You will need an API key to run it. The app is already hooked up to include it from the file "gradle.properties". You must insert a line at the end of the file such as: API_KEY="YOUR API KEY" in order to query the web services.

This app consists of two stages. 

Stage 1:
--------
- Present the user with a grid arrangement of movie posters upon launch.
- Allow your user to change sort order via a setting: The sort order can be by most popular or by highest-rated.
- Allow the user to tap on a movie poster and transition to a details screen with additional information such as: original title movie poster image thumbnail.
- A plot synopsis.
- A user rating.
- Release date.

Stage 2:
--------
- Allow users to view and play trailers ( either in the youtube app or a web browser).
- Allow users to read reviews of a selected movie.
- Allow users to mark a movie as a favorite in the details view by tapping a button. This is for a local movies collection that to maintain and does not require an API request.
- Modify the existing sorting criteria for the main view to include an additional pivot to show their favorites collection.
