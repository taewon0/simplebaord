package com.mysite.sbb.service;

import com.mysite.sbb.domain.Answer;
import com.mysite.sbb.domain.Question;
import com.mysite.sbb.domain.User.SiteUser;
import com.mysite.sbb.exception.DataNotFoundException;
import com.mysite.sbb.repository.QuestionRepository;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    /*public List<Question> getList(){
        return questionRepository.findAll();
    }*/

    public Page<Question> getList(int page, String kw){
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page,10, Sort.by(sorts));
        Specification<Question> spec = search(kw);
        //return questionRepository.findAll(spec, pageable);
        return questionRepository.findAllByKeyword(kw, pageable);
    }

    public Question getQuestion(Integer id){
        Optional<Question> question = questionRepository.findById(id);
        if(question.isPresent()){
            return question.get();
        } else{
            throw new DataNotFoundException("question not found");
        }
    }

    public void create(String subject, String content, SiteUser author){
        Question question = new Question();
        question.setSubject(subject);
        question.setContent(content);
        question.setCreateDate(LocalDateTime.now());
        question.setAuthor(author);
        questionRepository.save(question);
    }

    public void modify(Question question, String subject, String content){
        question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());
        questionRepository.save(question);
    }

    public void delete(Question question){
        questionRepository.delete(question);
    }

    public void vote(Question question, SiteUser siteUser){
        question.getVoter().add(siteUser);
        questionRepository.save(question);
    }

    private Specification<Question> search(String kw){
        return new Specification<Question>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                query.distinct(true);
                Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
                Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
                Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
                return criteriaBuilder.or(criteriaBuilder.like(q.get("subject"),"%" + kw + "%"),
                        criteriaBuilder.like(q.get("content"),"%" + kw + "%"),
                        criteriaBuilder.like(u1.get("username"),"%" + kw + "%"),
                        criteriaBuilder.like(a.get("content"),"%" + kw + "%"),
                        criteriaBuilder.like(u2.get("username"),"%" + kw + "%"));
            }
        };
    }
}
