package edu.cit.amihan.medibook.clinicstaff.entity;

import edu.cit.amihan.medibook.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clinic_staff")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long staffId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "position")
    private String position;
}
