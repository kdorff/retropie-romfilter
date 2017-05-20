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
        ref('standardAnalyzer'),
        [
            'system': ref('keywordAnalyzer'),
            'scrapeId': ref('keywordAnalyzer'),
            'path': ref('keywordAnalyzer'),
            'image': ref('keywordAnalyzer'),
            'thumbnail': ref('keywordAnalyzer'),
            'nameOrder': ref('keywordAnalyzer'),
            'developerOrder': ref('keywordAnalyzer'),
            'publisherOrder': ref('keywordAnalyzer'),
            'genreOrder': ref('keywordAnalyzer'),
            'regionOrder': ref('keywordAnalyzer'),
            'romtypeOrder': ref('keywordAnalyzer'),
        ]
    )

    gamesIndexDir(SimpleFSDirectory, Paths.get(application.config.retropie.romfilter.gamesIndexPath))
    gamesWriterConfig(IndexWriterConfig,
        ref('queryAnalyzer'))
    gamesIndexWriter(IndexWriter,
        ref('gamesIndexDir'), ref('gamesWriterConfig'))
}
