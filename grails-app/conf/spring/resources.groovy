import org.apache.lucene.analysis.core.KeywordAnalyzer
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.SimpleFSDirectory

import java.nio.file.Paths

// Place your Spring DSL code here
beans = {
    standardAnalyzer(StandardAnalyzer)
    keywordAnalyzer(KeywordAnalyzer)
    queryAnalyzer(PerFieldAnalyzerWrapper,
        ref('standardAnalyzer'), [
        'system': ref('keywordAnalyzer'),
        'scrapeId': ref('keywordAnalyzer'),
        'path': ref('keywordAnalyzer'),
        'image': ref('keywordAnalyzer'),
        'thumbnail': ref('keywordAnalyzer'),
        'gamelistEntryHash': ref('keywordAnalyzer'),
    ])

    systemsIndexDir(SimpleFSDirectory, Paths.get(application.config.retropie.romfilter.systemsIndexPath))
    systemsWriterConfig(IndexWriterConfig,
        ref('queryAnalyzer'))
    systemsIndexWriter(IndexWriter,
        ref('systemsIndexDir'), ref('systemsWriterConfig'))

    gamesIndexDir(SimpleFSDirectory, Paths.get(application.config.retropie.romfilter.gamesIndexPath))
    gamesWriterConfig(IndexWriterConfig,
        ref('queryAnalyzer'))
    gamesIndexWriter(IndexWriter,
        ref('gamesIndexDir'), ref('gamesWriterConfig'))

    romsIndexDir(SimpleFSDirectory, Paths.get(application.config.retropie.romfilter.romsIndexPath))
    romsWriterConfig(IndexWriterConfig,
        ref('queryAnalyzer'))
    romsIndexWriter(IndexWriter,
        ref('romsIndexDir'), ref('romsWriterConfig'))
}
