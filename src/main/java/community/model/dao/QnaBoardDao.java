package community.model.dao;

import static common.JdbcTemplate.*;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import common.JdbcTemplate;
import community.model.dto.Attachment;
import community.model.dto.QnaBoard;
import community.model.dto.QnaBoardComment;
import community.model.dto.QnaBoardExt;
import community.model.exception.QnaBoardException;



public class QnaBoardDao {
	private Properties prop = new Properties();
	public static QnaBoardDao getInstance() {
		return getInstance();
	}

	public QnaBoardDao() {
		String fileName = QnaBoardDao.class.getResource("/sql/qna-query.properties").getPath();
		try {
			prop.load(new FileReader(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 목록조회
	public List<QnaBoardExt> findAll(Connection conn, Map<String, Object> param) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		List<QnaBoardExt> list = new ArrayList<>();
		String sql = prop.getProperty("findAll");
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, (int) param.get("start"));
			pstmt.setInt(2, (int) param.get("end"));
			rset = pstmt.executeQuery();
			while (rset.next()) {
				QnaBoardExt board = handleBoardResultSet(rset);
				board.setAttachCount(rset.getInt("attach_cnt"));
				board.setCommentCount(rset.getInt("comment_cnt"));
				list.add(board);
			}

		} catch (Exception e) {
			throw new QnaBoardException("글 목록 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}
		return list;
	}

	private QnaBoardExt handleBoardResultSet(ResultSet rset) throws SQLException {
		QnaBoardExt board = new QnaBoardExt();
		board.setNo(rset.getInt("board_no"));
		board.setMemberId(rset.getString("member_id"));
		board.setTitle(rset.getString("board_title"));
		board.setContent(rset.getString("content"));
		board.setReadCount(rset.getInt("read_count"));
		board.setRegDate(rset.getDate("reg_date"));
		return board;
	}

	public int getTotalContents(Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		int totalContents = 0;
		String sql = prop.getProperty("getTotalContents");
		try {
			pstmt = conn.prepareStatement(sql);
			rset = pstmt.executeQuery();
			while (rset.next()) {
				totalContents = rset.getInt(1);
			}
		} catch (Exception e) {
			throw new QnaBoardException("총 게시물수 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}
		return totalContents;
	}

	public int insertBoard(Connection conn, QnaBoard board) {
		PreparedStatement pstmt = null;
		int result = 0;
		String sql = prop.getProperty("insertBoard");
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, board.getMemberId());
			pstmt.setString(2, board.getTitle());
			pstmt.setString(3, board.getContent());
			result = pstmt.executeUpdate();
		} catch (Exception e) {
			throw new QnaBoardException("게시글 등록 오류", e);
		} finally {
			close(pstmt);
		}
		return result;
	}

	public int findCurrentBoardNo(Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		int no = 0;
		String sql = prop.getProperty("findCurrentBoardNo");

		try {
			pstmt = conn.prepareStatement(sql);
			rset = pstmt.executeQuery();
			while (rset.next())
				no = rset.getInt(1);

		} catch (SQLException e) {
			throw new QnaBoardException("게시글 번호 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}
		return no;
	}

	public int insertAttachment(Connection conn, Attachment attach) {
		PreparedStatement pstmt = null;
		int result = 0;
		String sql = prop.getProperty("insertAttachment");
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, attach.getBoardNo());
			pstmt.setString(2, attach.getOriginalFilename());
			pstmt.setString(3, attach.getRenamedFilename());
			result = pstmt.executeUpdate();
		} catch (Exception e) {
			throw new QnaBoardException("첨부파일 등록 오류", e);
		} finally {
			close(pstmt);
		}
		return result;
	}

	public QnaBoardExt findByNo(Connection conn, int no) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		QnaBoardExt board = null;
		String sql = prop.getProperty("findByNo");

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, no);
			rset = pstmt.executeQuery();
			while (rset.next()) {
				board = handleBoardResultSet(rset);
			}
		} catch (SQLException e) {
			throw new QnaBoardException("게시글 한건 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}

		return board;
	}

	public List<Attachment> findAttachmentByBoardNo(Connection conn, int no) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		List<Attachment> attachments = new ArrayList<>();
		String sql = prop.getProperty("findAttachmentByBoardNo");

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, no);
			rset = pstmt.executeQuery();
			while (rset.next()) {
				Attachment attach = handleAttachmentResultSet(rset);
				attachments.add(attach);
			}
		} catch (SQLException e) {
			throw new QnaBoardException("게시글 번호에 의한 첨부파일조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}

		return attachments;
	}

	private Attachment handleAttachmentResultSet(ResultSet rset) throws SQLException {
		Attachment attach = new Attachment();
		attach.setNo(rset.getInt("no"));
		attach.setBoardNo(rset.getInt("board_no"));
		attach.setOriginalFilename(rset.getString("original_filename"));
		attach.setRenamedFilename(rset.getString("renamed_filename"));
		attach.setRegDate(rset.getDate("reg_date"));
		return attach;
	}

	public int updateReadCount(Connection conn, int no) {
		PreparedStatement pstmt = null;
		int result = 0;
		String sql = prop.getProperty("updateReadCount");
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, no);
			result = pstmt.executeUpdate();
		} catch (Exception e) {
			throw new QnaBoardException("조회수 증가처리 오류", e);
		} finally {
			close(pstmt);
		}
		return result;
	}

	public Attachment findAttachmentByNo(Connection conn, int no) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		Attachment attach = null;
		String sql = prop.getProperty("findAttachmentByNo");

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, no);
			rset = pstmt.executeQuery();
			if (rset.next())
				attach = handleAttachmentResultSet(rset);

		} catch (SQLException e) {
			throw new QnaBoardException("첨부파일 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}
		return attach;
	}

	public int deleteBoard(Connection conn, int no) {
		int result = 0;
		PreparedStatement pstmt = null;
		String query = prop.getProperty("deleteBoard");

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, no);
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new QnaBoardException("게시글 삭제 오류", e);
		} finally {
			close(pstmt);
		}

		return result;
	}

	public int updateBoard(Connection conn, QnaBoardExt board) {
		PreparedStatement pstmt = null;
		int result = 0;
		String sql = prop.getProperty("updateBoard");
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, board.getTitle());
			pstmt.setString(2, board.getContent());
			pstmt.setInt(3, board.getNo());
			result = pstmt.executeUpdate();
		} catch (Exception e) {
			throw new QnaBoardException("게시글 수정 오류", e);
		} finally {
			close(pstmt);
		}
		return result;
	}

	public int deleteAttachment(Connection conn, int no) {
		int result = 0;
		PreparedStatement pstmt = null;
		String query = prop.getProperty("deleteAttachment");

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, no);
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new QnaBoardException("첨부파일 삭제 오류", e);
		} finally {
			close(pstmt);
		}
		return result;
	}

	public int insertBoardComment(Connection conn, QnaBoardComment bc) {
		int result = 0;
		PreparedStatement pstmt = null;
		String query = prop.getProperty("insertBoardComment");

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, bc.getBoardNo());
			pstmt.setString(2, bc.getMemberId());
			pstmt.setString(3, bc.getContent());
			pstmt.setInt(4, bc.getCommentLevel());
			pstmt.setObject(5, bc.getCommentRef() == 0 ? null : bc.getCommentRef());

			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new QnaBoardException("댓글 등록 오류", e);
		} finally {
			close(pstmt);
		}
		return result;
	}

	public List<QnaBoardComment> findBoardCommentByBoardNo(Connection conn, int no) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		List<QnaBoardComment> comments = new ArrayList<>();
		String sql = prop.getProperty("findBoardCommentByBoardNo");

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, no);
			rset = pstmt.executeQuery();
			while (rset.next()) {
				QnaBoardComment bc = new QnaBoardComment();
				bc.setNo(rset.getInt("comment_no"));
				bc.setCommentLevel(rset.getInt("comment_level"));
				bc.setBoardNo(rset.getInt("board_no"));
				bc.setMemberId(rset.getString("member_id"));
				bc.setContent(rset.getString("content"));
				bc.setCommentRef(rset.getInt("comment_ref"));
				bc.setLikeCnt(rset.getInt("like_count"));
				bc.setRegDate(rset.getDate("reg_date"));
				comments.add(bc);
			}

		} catch (SQLException e) {
			throw new QnaBoardException("댓글 목록 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}

		return comments;
	}

	public int deleteBoardComment(Connection conn, int no) {
		PreparedStatement pstmt = null;
		int result = 0;
		String sql = prop.getProperty("deleteBoardComment");
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, no);
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new QnaBoardException("댓글 삭제 오류", e);
		} finally {
			close(pstmt);
		}
		return result;
	}

	public List<QnaBoard> findBy(Connection conn, Map<String, String> param) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		List<QnaBoard> list = new ArrayList<>();
		String sql = prop.getProperty("findBy");
		sql = sql.replace("#", param.get("searchType"));
		System.out.println("sql = " + sql);
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "%" + param.get("searchKeyword") + "%");
			rset = pstmt.executeQuery();
			while (rset.next()) {
				QnaBoard QnaBoard = handleBoardResultSet(rset);
				list.add(QnaBoard);
			}
		} catch (Exception e) {
			throw new QnaBoardException("질문게시글 검색 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}
		return list;
	}

	// 댓글0인 게시글
	public List<QnaBoard> noComment(Connection conn) {
		List<QnaBoard> list = new ArrayList<QnaBoard>();
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		String sql = prop.getProperty("noComment");

		// 채워야할 ? 없음
		try {
			pstmt = conn.prepareStatement(sql);
			rset = pstmt.executeQuery();

			while (rset.next()) {
				QnaBoard qb = new QnaBoard();
				qb.setNo(rset.getInt("board_no"));
				qb.setMemberId(rset.getString("member_id"));
				qb.setTitle(rset.getString("board_title"));
				qb.setContent(rset.getString("content"));
				qb.setReadCount(rset.getInt("read_count"));
				qb.setRegDate(rset.getDate("reg_date"));
				list.add(qb);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rset);
			close(pstmt);
		}
		System.out.println(list);
		return list;
	}

	// 댓글 수
	public int commentCount(Connection conn, int no) {
		List<QnaBoardComment> list = new ArrayList<>();
		QnaBoardComment bc = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		String sql = prop.getProperty("commentCount");

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, no);
			rset = pstmt.executeQuery();
			while (rset.next()) {
				bc = new QnaBoardComment();

				bc.setNo(rset.getInt("comment_no"));
				bc.setCommentLevel(rset.getInt("comment_level"));
				bc.setBoardNo(rset.getInt("board_no"));
				bc.setMemberId(rset.getString("member_id"));
				bc.setContent(rset.getString("content"));
				bc.setCommentRef(rset.getInt("comment_ref"));
				bc.setLikeCnt(rset.getInt("like_count"));
				bc.setRegDate(rset.getDate("reg_date"));

				list.add(bc);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return list.size();
	}

//좋아요 
	private int insert(Connection conn, QnaBoardComment bc) {
		int n = 0;
		PreparedStatement pstmt = null;
		String sql = prop.getProperty("likeComment");
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bc.getNo());
			n = pstmt.executeUpdate();
			System.out.println(n + "개의 좋아요 등록 완료");
			
			if(n != 0) {
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, bc.getNo());
				n = pstmt.executeUpdate();
				System.out.println(n + "건의 가게 정보 좋아요수 +1 완료");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return n;
	}
	
	private int delete(Connection conn, QnaBoardComment bc) {
		int n = 0;
		PreparedStatement pstmt = null;
		String sql1 = "delete from qa_board_reply where comment_no=? and member_id=?";
		String sql2 = "update qa_board_reply set like_count=like_count-1 where comment_no=?";
		try {
			pstmt = conn.prepareStatement(sql1);
			pstmt.setInt(1, bc.getNo());
			pstmt.setString(2, bc.getMemberId());
			n = pstmt.executeUpdate();
			System.out.println(n + "건의 좋아요 등록 삭제 완료");
			
			if(n != 0) {
				pstmt = conn.prepareStatement(sql2);
				pstmt.setInt(1, bc.getNo());
				n = pstmt.executeUpdate();
				System.out.println(n + "건의 가게 정보 좋아요수 -1 완료");				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return n;
	}

	public int checkInsert(Connection conn, QnaBoardComment bc) {
		int likeCount = 0;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		String sql = "select count(*) from qa_board_reply where comment_no=? and member_id=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bc.getNo());
			pstmt.setString(2, bc.getMemberId());
			rset = pstmt.executeQuery();
			if(rset.next()) {
				likeCount = rset.getInt(1);
			}
			if(likeCount == 0) {
				insert(conn, bc);
			} else {
				delete(conn, bc);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return likeCount;
	}
	
	public int selectStoreNum(Connection conn, int no){
		int likeCount = 0;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		String sql = "select count(*) as like_count from qa_board_reply where comment_no= ?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, no);
			rset = pstmt.executeQuery();
			if(rset.next()) {
				likeCount = rset.getInt("like_count");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return likeCount;
	}
}
