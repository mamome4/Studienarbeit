package com.example.studienarbeit_demo.dao;

import com.example.studienarbeit_demo.model.Student;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository("fakeDao")
public class FakeStudentDataAccessService implements StudentDao{

    private static List<Student> DB = new ArrayList<>();

    @Override
    public int insertStudent(UUID id, Student student) {
        DB.add(new Student(id, student.getName(), student.getFirstname(), student.getEmail()));
        return 1;
    }
    public List<Student> selectAllStudents() {
        return DB;
    }

}
