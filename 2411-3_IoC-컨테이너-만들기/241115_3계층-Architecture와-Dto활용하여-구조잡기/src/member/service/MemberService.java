package member.service;

import member.model.Member;
import member.model.MemberCreate;

import java.util.List;

public interface MemberService {
    Member join(MemberCreate memberCreate);
    Member getById(Long id);
    List<Member> getAll();
}
