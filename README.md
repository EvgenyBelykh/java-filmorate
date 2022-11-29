# java-filmorate
Template repository for Filmorate project.
![diagram](https://github.com/EvgenyBelykh/java-filmorate/blob/main/filmorate.png)

## Code from dbdiagram.io:



  ``` 
  Project filmorate {
  database_type: 'H2'
}

Table users as U{
  id int [pk, increment]
  name varchar
  email varchar [unique]
  login varchar [not null]
  birthday timestamp
}

Table films as F {
  id int [pk, increment]
  name varchar
  description varchar
  release_date timestamp
  duration int
  rate double
  mpa varchar
}
  
Table film_genres {
  id_film int [pk]
  id_genre int [pk]
}

Table genres {
  id int [pk]
  name genre
}

Table mpa {
  id int [pk]
  name mpa
}

Table rate {
  id_film int [pk]
  id_user int [pk]
  rate int
}

Table users_friends{
  id_user_one int [pk]
  id_user_two int [pk]
}

Table film_direcors{
  id_film int [pk]
  id_director int [pk]
}

Table direcors{
  id int [pk]
  name varchar
}

Table events{
  event_id int [pk]
  user_id int
  entity_id int
  event_type varchar
  operation varchar
  time int
}

Table reviews{
  review_id int [pk]
  film_id int
  user_id int
  content varchar
  usefull int
  is_positive boolean
}

Table review_dislike{
  user_id int [pk]
  review_id int
}

Table review_like{
  user_id int [pk]
  review_id int
}

Ref: U.id < rate.id_user
Ref: F.id < rate.id_film
Ref: U.id < users_friends.id_user_one
Ref: U.id < users_friends.id_user_two
Ref: genres.id < film_genres.id_genre
Ref: F.id < film_genres.id_film
Ref: mpa.id > F.mpa
Ref: films.id < film_direcors.id_film
Ref: film_direcors.id_director > direcors.id
Ref: events.user_id > users.id
Ref: reviews.review_id < review_like.review_id
Ref: reviews.review_id < review_dislike.review_id
Ref: users.id - reviews.user_id
Ref: users.id < review_dislike.user_id
Ref: users.id < review_like.user_id
Ref: reviews.film_id - films.id
   ```
