package com.douban.book

import com.douban.base.{Constant, DoubanListFragment, DoubanActivity}
import android.os.Bundle
import com.douban.models.{AnnotationSearch, Book}
import android.view.{ViewGroup, View}
import scala.concurrent._
import android.content.Context
import android.widget.{BaseAdapter, SimpleAdapter}
import ExecutionContext.Implicits.global
import scala.collection.mutable
import collection.JavaConverters._

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 10/2/13 9:32 PM
 * @version 1.0
 */
class NotesActivity extends DoubanActivity {
  lazy val bookId = getIntent.getLongExtra(Constant.BOOK_ID, 0)
  var notesListFragment: Option[NotesListFragment] = None

  protected override def onCreate(b: Bundle) {
    super.onCreate(b)
    if (0 == bookId) finish()
    setContentView(R.layout.notes)
    notesListFragment = Some(new NotesListFragment())
    getFragmentManager.beginTransaction().replace(R.id.notes_list, notesListFragment.get).commit()
  }

  def search(v: View) = notesListFragment.get.search(v)
}

class NotesListFragment extends DoubanListFragment[NotesActivity] {
  import R.id._
  var currentPage = 1
  var mapping=Map(page_num->"page_no",chapter_name->"chapter",note_time->"time",username->"author_user.name",note_content->"content",user_avatar->("author_user.avatar",("author_user.name",getString(R.string.load_img_fail))))

  override def onCreate(b: Bundle) {
    super.onCreate(b)
    val a = Book.annotationsOf(getThisActivity.bookId)
  }

  def search(bookId: Long = getThisActivity.bookId, order: String = "rank", page: Int = currentPage) = future {
    val a = Book.annotationsOf(bookId, new AnnotationSearch(order = order, page = page))
    getThisActivity.setTitle(getString(R.string.annotation) + s"(${a.start + a.annotations.size}/${a.total}})")
  }

  def search(v: View) {
    loadData(v)
  }

  def loadData(v: View,page:Int=1) {
    val order = Map(R.id.rank -> "rank", R.id.collect -> "collect", R.id.page -> "page")
    v.getId match {
      case id: Int if order.contains(id) => {
        v.setBackgroundColor(R.color.black_light)
        order.keys.filter(_ != id).foreach(rootView.findViewById(_).setBackgroundColor(R.color.black))
        currentPage = page
        search(order = order(id))
      }
    }
  }

  def load(v: View){
    loadData(v,currentPage+1)
  }


  class NoteItemAdapter(context: Context, data: mutable.Buffer[Map[String, String]]) extends BaseAdapter {
    override def getView(position: Int, view: View, parent: ViewGroup): View = {
      val convertView = if(null!=view) view else ctx.getLayoutInflater.inflate(R.layout.notes_item,null)
      val currentLine: Map[String, String] = data.get(position)
      batchSetValues(mapping,currentLine,convertView)
      convertView
    }

    def getCount: Int = data.length

    def getItem(p1: Int): Map[String,String] =data.get(p1)

    def getItemId(p1: Int): Long = p1
  }

}

