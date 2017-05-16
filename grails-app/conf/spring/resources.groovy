import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.SimpleFSDirectory

import java.nio.file.Paths

// Place your Spring DSL code here
beans = {
    queryAnalyzer(StandardAnalyzer)

    systemsIndexDir(SimpleFSDirectory, Paths.get('./systems-index'))
    systemsWriterConfig(IndexWriterConfig,
        ref('queryAnalyzer'))
    systemsIndexWriter(IndexWriter,
        ref('systemsIndexDir'), ref('systemsWriterConfig'))

    gamesIndexDir(SimpleFSDirectory, Paths.get('./games-index'))
    gamesWriterConfig(IndexWriterConfig,
        ref('queryAnalyzer'))
    gamesIndexWriter(IndexWriter,
        ref('gamesIndexDir'), ref('gamesWriterConfig'))

    romsIndexDir(SimpleFSDirectory, Paths.get('./roms-index'))
    romsWriterConfig(IndexWriterConfig,
        ref('queryAnalyzer'))
    romsIndexWriter(IndexWriter,
        ref('romsIndexDir'), ref('romsWriterConfig'))
}
