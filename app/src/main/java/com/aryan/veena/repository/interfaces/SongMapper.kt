import com.aryan.veena.repository.datamodels.NowPlayingModel

interface SongMapper<T> {
    fun map(item: T): NowPlayingModel
}
