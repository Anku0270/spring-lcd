package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "information")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Information {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private InfoType type;

    @Column(name = "file_url")
    private String url;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonBackReference
    private Profile profile;


}
