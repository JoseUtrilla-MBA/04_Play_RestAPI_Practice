package products.data.repositories

import play.api.MarkerContext
import products.controller.RequestMarkerContext

trait Repository  [T] extends RequestMarkerContext {

  def list()(implicit mc: MarkerContext): List[T]

  def get(id: Int)(implicit mc: MarkerContext): Option[T]

  def create(data: T)(implicit mc: MarkerContext): Int

  def delete(id: Int)(implicit mc: MarkerContext): List[T]

}
