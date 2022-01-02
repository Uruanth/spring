package co.com.sofka.springsideclient.spring.controller;

import co.com.sofka.springsideclient.spring.modelos.Person;
import co.com.sofka.springsideclient.spring.repositorio.PersonRepository;
import co.com.sofka.springsideclient.spring.servicio.PersonService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = PersonController.class)
class PersonControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    //Pruebas funcionales
    @MockBean
    private PersonRepository repository;

    //Spiar los datos del service
    @SpyBean
    private PersonService personService;

    //Capturar los datos del servicio
    @Captor
    private ArgumentCaptor<Person> argumentCaptor;

    @ParameterizedTest
    @CsvSource(
            {"Raul Alzate,0",
            "Raul Alzate,1" })
    void post(String name, Integer times){

        if(times == 0) {
            when(repository.findByName(name)).thenReturn(Mono.just(new Person()));
        }

        if(times == 1)
        {
            when(repository.findByName(name)).thenReturn(Mono.empty());
        }

        var request = Mono.just(new Person(name));

        when(repository.save(any())).thenReturn(Mono.empty());
        // when(repository.findByName(name)).thenReturn(Mono.empty());
        // when(repository.findByName(name)).thenReturn(Mono.just(new Person(name)));

        webTestClient.post()
                .uri("/person")
                .body(request, Person.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        verify(personService).insert(argumentCaptor.capture());
        verify(repository).findByName(name);
        verify(repository, Mockito.times(times)).save(any());


        var person = argumentCaptor.getValue();
        Assertions.assertEquals(name, person.getName());

    }


    @Test
    void get(){
        var personTest = new Person("Raul");
        var id = "1";
        when(repository.findById(id)).thenReturn(Mono.just(personTest));


        webTestClient.get()
                .uri("/person/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Person.class)
                .consumeWith(personEntityExchangeResult -> {
                    var person = personEntityExchangeResult.getResponseBody();
                    assert person != null;
                });

        Mockito.verify(repository).findById(id);
    }


    @Test
    void update(){

        var nameUpdate = "Alzate";
        var personSave = new Person(nameUpdate);
        var request = Mono.just(new Person(nameUpdate));

        //when(repository.findByName(name)).thenReturn(response);
        when(repository.save(any())).thenReturn(request);

        webTestClient.put()
                .uri("/person")
                .body(request, Person.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(personEntityExchangeResult -> {
                    var person = personEntityExchangeResult.getResponseBody();
                    assert person != null;
                });
        Mockito.verify(repository).save(refEq(personSave));
    }


    @Test
    void delete(){

        Person persona = new Person("Raul");
        Mono<Person> request = Mono.just(persona);
        when(repository.findById("1")).thenReturn(request);
        when(repository.delete(persona)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/person/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        Mockito.verify(repository).findById("1");
        Mockito.verify(repository).delete(refEq(persona));
    }

    @Test
    void list() {
        var list = Flux.just(
                new Person("Raul Alzate"),
                new Person("Pedro" )
        );
        when(repository.findAll()).thenReturn(list);

        webTestClient.get()
                .uri("/person")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Raul Alzate")
                .jsonPath("$[1].name").isEqualTo("Pedro");

        verify(personService).listAll();
        verify(repository).findAll();

    }


}