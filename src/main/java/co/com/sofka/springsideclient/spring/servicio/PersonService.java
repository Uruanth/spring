package co.com.sofka.springsideclient.spring.servicio;

import co.com.sofka.springsideclient.spring.modelos.Person;
import co.com.sofka.springsideclient.spring.repositorio.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.BiFunction;

@Service
public class PersonService {

    @Autowired
    PersonRepository repository;

    public Flux<Person> listAll() {
        return repository.findAll();
    }

    public Mono<Void> insert(Person person) {
        return validateBeforeInsert.apply(repository, person)
                .switchIfEmpty(Mono.defer(() -> repository.save(person)))
                .then();

        //return repository.save(capture).then();
        //.flatMap(repository::save)
    }


    public Mono<Person> getById(String id) {
        return repository.findById(id);
    }

    public Mono<Person> update(Mono<Person> person) {
        return person.flatMap(persona -> {
            System.out.println("persona = " + persona);
            return repository.save(persona);
        });
    }


    private final BiFunction<PersonRepository, Person, Mono<Person>> validateBeforeInsert
            = (repo, person) -> repo.findByName(person.getName());


    public Mono<Void> delete(String id) {
        return getById(id).flatMap(persona -> {
                    repository.delete(persona);
                    return Mono.just(persona);
                })
                .then();
    }
}
