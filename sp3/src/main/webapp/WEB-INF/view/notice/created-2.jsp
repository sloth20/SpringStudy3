<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%
   String cp = request.getContextPath();
%>

<script type="text/javascript">
function sendOk() {
	var f = document.boardForm;

	var str = f.subject.value;
	if(!str) {
		alert("제목을 입력하세요. ");
		f.subject.focus();
		return;
	}

	str = f.content.value;
	if(!str) {
		alert("내용을 입력하세요. ");
		f.content.focus();
		return;
	}

	f.action="<%=cp%>/notice/${mode}";
		
	f.submit();
}



<c:if test = "${mode=='update'}">
	function deleteFile(fileNum){
		var url = "<%=cp%>/notice/deleteFile";
		$.post(url,{fileNum:fileNum},function(data){
			$("#fileTr"+fileNum).remove();
		},"json");
	}
</c:if>


</script>

<div class="body-container" style="width: 700px;">
	<div class="body-title">
		<h3><span style="font-family: Webdings">2</span> 공지사항 </h3>
	</div>

	<div>
		<form name="boardForm" method="post" enctype="multipart/form-data">
		<table style="width: 100%; margin: 20px auto 0px; border-spacing: 0px; border-collapse: collapse;">
			<tbody id="noticetb">
			
			<tr align="left" height="40" style="border-top: 1px solid #cccccc; border-bottom: 1px solid #cccccc;"> 
				<td width="100" bgcolor="#eeeeee" style="text-align: center;">제&nbsp;&nbsp;&nbsp;&nbsp;목</td>
				<td style="padding-left:10px;">
					<input type="text" name="subject" maxlength="100" class="boxTF" style="width: 95%;" value="${dto.subject}">
				</td>
			</tr>

			<tr align="left" height="40" style="border-bottom: 1px solid #cccccc;"> 
				<td width="100" bgcolor="#eeeeee" style="text-align: center;">공지여부</td>
				<td style="padding-left:10px;">
					<label><input type="checkbox" name="notice" value="1" ${dto.notice==1?"checked='checked'":""}> 공지</label>
				</td>
			</tr>
			<tr align="left" height="40" style="border-bottom: 1px solid #cccccc;"> 
				<td width="100" bgcolor="#eeeeee" style="text-align: center;">작성자</td>
				<td style="padding-left:10px;">
					${sessionScope.member.userName}
				</td>
			</tr>
			

			<tr align="left" style="border-bottom: 1px solid #cccccc;">
				<td width="100" bgcolor="#eeeeee" style="text-align: center; padding-top:5px;" valign="top">내&nbsp;&nbsp;&nbsp;&nbsp;용</td>
				<td valign="top" style="padding:5px 0px 5px 10px;"> 
					<textarea name="content" rows="12" class="boxTA" style="width: 95%;">${dto.content }</textarea>
				</td>
			</tr>
			
			<tr align="left" height="40" style="border-bottom: 1px solid #cccccc;">
				<td width="100" bgcolor="#eeeeee" style="text-align: center;">첨&nbsp;&nbsp;&nbsp;&nbsp;부</td>
				<td style="padding-left:10px;"> 
					<input type="file" name="upload" multiple="multiple" class="boxTF" size="53" style="height: 25px;">
				</td>
			</tr> 
			</tbody>
			<c:if test="${mode=='update'}">
				<c:forEach var="vo" items="${listFile }">
					<tr id="fileTr${vo.fileNum}" align="left" height="40" style="border-bottom: 1px solid #cccccc;"> 
						<td width="100" bgcolor="#eeeeee" style="text-align: center;">첨부된 파일</td>
						<td style="padding-left:10px;">
							<a href="javascript:deleteFile('${vo.fileNum }')"><i class="far fa-trash-alt"></i></a>&nbsp;${vo.originalFilename}						
						</td>
					</tr>
			
				
				</c:forEach>
			</c:if>
		</table>

		<table style="width: 100%; border-spacing: 0px;">
			<tr height="45">
				<td align="center" >
					<c:if test="${mode=='update'}">
						<input type="hidden" name="num" value="${dto.num}">
						<input type="hidden" name="page" value="${page}">
										</c:if>
					<button type="button" class="btn" onclick="sendOk();">${mode=='update'?'수정완료':'등록하기'}</button>
					<button type="reset" class="btn">다시입력</button>
					<button type="button" class="btn" onclick="javascript:location.href='<%=cp%>/notice/list';">${mode=='update'?'수정취소':'등록취소'}</button>
				</td>
			</tr>
			
		</table>
		</form>
	</div>

</div>