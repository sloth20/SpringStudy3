package com.sp.member;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sp.common.dao.CommonDAO;
import com.sp.mail.Mail;
import com.sp.mail.MailSender;

@Service("member.memberService")
public class MemberServiceImpl implements MemberService {
	@Autowired
	private CommonDAO dao;

	@Autowired
	private MailSender mailSender;

	@Override
	public Member loginMember(String userId) {
		Member dto = null;

		try {
			dto = dao.selectOne("member.loginMember", userId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dto;
	}

	@Override
	public void insertMember(Member dto) throws Exception {
		try {
			if (dto.getEmail1() != null && dto.getEmail1().length() != 0 && dto.getEmail2() != null
					&& dto.getEmail2().length() != 0)
				dto.setEmail(dto.getEmail1() + "@" + dto.getEmail2());

			if (dto.getTel1() != null && dto.getTel1().length() != 0 && dto.getTel2() != null
					&& dto.getTel2().length() != 0 && dto.getTel3() != null && dto.getTel3().length() != 0)
				dto.setTel(dto.getTel1() + "-" + dto.getTel2() + "-" + dto.getTel3());

			long memberSeq = dao.selectOne("member.memberSeq");
			dto.setMemberIdx(memberSeq);

			// 회원정보 저장
			dao.insertData("member.insertMember", memberSeq);

			// dao.insertData("member.insertMember1", dto);
			// dao.insertData("member.insertMember2", dto);
			dao.updateData("member.insertMember12", dto); // member1, member2 테이블 동시에
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public Member readMember(String userId) {
		Member dto = null;

		try {
			dto = dao.selectOne("member.readMember", userId);

			if (dto != null) {
				if (dto.getEmail() != null) {
					String[] s = dto.getEmail().split("@");
					dto.setEmail1(s[0]);
					dto.setEmail2(s[1]);
				}

				if (dto.getTel() != null) {
					String[] s = dto.getTel().split("-");
					dto.setTel1(s[0]);
					dto.setTel2(s[1]);
					dto.setTel3(s[2]);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return dto;
	}

	@Override
	public Member readMember(long memberIdx) {
		Member dto = null;

		try {
			dto = dao.selectOne("member.readMember2", memberIdx);

			if (dto != null) {
				if (dto.getEmail() != null) {
					String[] s = dto.getEmail().split("@");
					dto.setEmail1(s[0]);
					dto.setEmail2(s[1]);
				}

				if (dto.getTel() != null) {
					String[] s = dto.getTel().split("-");
					dto.setTel1(s[0]);
					dto.setTel2(s[1]);
					dto.setTel3(s[2]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dto;
	}

	@Override
	public void updateMembership(Map<String, Object> map) throws Exception {
		try {
			dao.updateData("member.updateMembership", map);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void updateLastLogin(String userId) throws Exception {
		try {
			dao.updateData("member.updateLastLogin", userId);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void updateMember(Member dto) throws Exception {
		try {
			if (dto.getEmail1() != null && dto.getEmail1().length() != 0 && dto.getEmail2() != null
					&& dto.getEmail2().length() != 0)
				dto.setEmail(dto.getEmail1() + "@" + dto.getEmail2());

			if (dto.getTel1() != null && dto.getTel1().length() != 0 && dto.getTel2() != null
					&& dto.getTel2().length() != 0 && dto.getTel3() != null && dto.getTel3().length() != 0)
				dto.setTel(dto.getTel1() + "-" + dto.getTel2() + "-" + dto.getTel3());

			dao.updateData("member.updateMember1", dto);
			dao.updateData("member.updateMember2", dto);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void deleteMember(Map<String, Object> map) throws Exception {
		try {
			map.put("membershep", 0);
			updateMembership(map);

			dao.deleteData("member.deleteMember2", map);
			dao.deleteData("member.deleteMember1", map);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public int dataCount(Map<String, Object> map) {
		int result = 0;

		return result;
	}

	@Override
	public List<Member> listMember(Map<String, Object> map) {
		List<Member> list = null;

		return list;
	}

	@Override
	public void generatePwd(Member dto) throws Exception {
		// 10 자리 임시 패스워드 생성
		StringBuilder sb = new StringBuilder();
		Random rd = new Random();
		String s = "!@#$%^&*~-+ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
		for (int i = 0; i < 10; i++) {
			int n = rd.nextInt(s.length());
			sb.append(s.substring(n, n + 1));
		}

		String result;
		result = dto.getUserId() + "님의 새로 발급된 임시 패스워드는 <b>" + sb.toString() + "</b> 입니다.<br>"
				+ "로그인 후 반드시 패스워드를 변경 하시기 바랍니다.";

		
		Mail mail = new Mail();
		mail.setReceiverEmail(dto.getEmail());

		mail.setSenderEmail("catdogincorporated@gmail.com");
		mail.setSenderName("관리자");
		mail.setSubject("임시 패스워드 발급");
		mail.setContent(result);

		boolean b = mailSender.mailSend(mail);
		System.out.println(b);
		if (b) {
			dto.setUserPwd(sb.toString());
			updateMember(dto);
		} else {
			throw new Exception("이메일 전송중 오류가 발생했습니다.");
		}
	}
}
