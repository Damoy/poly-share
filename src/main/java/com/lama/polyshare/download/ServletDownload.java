package com.lama.polyshare.download;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.storage.Blob;
import com.lama.polyshare.commons.Utils;
import com.lama.polyshare.upload.CloudStorageHelper;


/***
 *	Servlet permettant le download des fichiers 
 * 	- Le Post permet la generation du lien de telechargement 
 * 	- Le get permet de telecharger un fichier via l'url de telechargement.
 */
@SuppressWarnings("serial")
public class ServletDownload extends HttpServlet  {

	private final static Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String mail = req.getParameter("mail");


		if(Utils.checkRequest(mail)) {
			String linkID = req.getParameter("linkId");

			Query<Entity> uploadQuery = Query.newEntityQueryBuilder().setKind("CustomsLinks")
					.setFilter(PropertyFilter.eq("id", linkID))
					.build();

			QueryResults<Entity> workingLinks = datastore.run(uploadQuery);

			if(workingLinks.hasNext()) {
				Entity metaFileDataToDownload = workingLinks.next();
				String fileName = metaFileDataToDownload.getString("fileName");
				CloudStorageHelper storageHelper =	new CloudStorageHelper();
				Blob b = storageHelper.downloadFile("staging.poly-share.appspot.com", fileName);

				resp.setCharacterEncoding("UTF-8");
				resp.setContentType("application/octet-stream");   
				resp.setHeader("Content-Disposition","attachment; filename=\"" + fileName + "\"");   

				try (OutputStream out = resp.getOutputStream()){
					out.write(b.getContent());
					out.flush();
				}catch(IOException e){

					e.printStackTrace();
				}
				Queue queue = QueueFactory.getDefaultQueue();
				queue.add(TaskOptions.Builder.withUrl("/worker/download").method(
						Method.GET).param("mail", mail));
			}
			else {
				resp.sendError(402);
			}
		} else {
			// mail ou resp.senderreur400 invalid operation pour son rang ou sa vie ou idk balec
			resp.sendError(400);
		}
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
	ServletException {

		String fileName = req.getParameter("fileName");
		String mail = req.getParameter("mail");
		String id = Timestamp.now().toString();
		
		if( fileName != null && mail != null ) {
			Queue queue = QueueFactory.getDefaultQueue();
			queue.add(TaskOptions.Builder.withUrl("/worker/download").method(Method.POST)
					.param("fileName",fileName).param("mail", mail).param("id",id ));
		}
		
		try {
			resp.getWriter().write("http://poly-share.appspot.com/Download?linkId=" + id +"&mail=" + mail+ "\n");
		} catch (Exception e) {
			throw new ServletException("Error updating book", e);
		}
	}
}
