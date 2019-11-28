<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	String cp = request.getContextPath();
%>
<c:forEach var="vo" items="${list}">
	<div class='answer' style='padding: 0px 10px;'>
		<div style='clear: both; padding: 10px 0px;'>
			<div style='float: left; width: 5%;'>└</div>
			<div style='float: left; width: 95%;'>
				<div style='float: left;'>
					<b>${vo.userName }</b>
				</div>
				<div style='float: right;'>
					<span>${vo.created }</span> |

					<c:choose>
						<c:when
							test="${vo.userId==sessionScope.member.userId||sessionScope.member.userId=='admin' }">
							<span class='deleteReplyAnswer' style='cursor: pointer;'
								data-replyNum='${vo.replyNum}' data-answer='${vo.answer}'>삭제</span>
						</c:when>
						<c:otherwise>
							<span class="notifyReply">신고</span>
						</c:otherwise>
					</c:choose>



				</div>
			</div>
		</div>
		<div
			style='clear: both; padding: 5px 5px 5px 5%; border-bottom: 1px solid #ccc;'>
			${vo.content }</div>
	</div>
</c:forEach>