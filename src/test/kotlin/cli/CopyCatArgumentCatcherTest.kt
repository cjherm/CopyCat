package cli

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import utility.Logger
import utility.TestResourceEnvironment
import java.io.File

class CopyCatArgumentCatcherTest {

    private val srcPath  = "src/test/resources/testSrc"
    private val destPath = "src/test/resources/testDest"
    private val compPath = "src/test/resources/testComp"

    @BeforeEach
    fun setUp() {
        TestResourceEnvironment.setup()
        Logger.logFile = null
        Logger.printToConsole = false
    }

    @AfterEach
    fun tearDown() {
        TestResourceEnvironment.teardown()
        Logger.logFile = null
        Logger.printToConsole = false
    }

    // -------------------------------------------------------------------------
    // requestsGui
    // -------------------------------------------------------------------------

    @Test
    fun `requestsGui returns true when gui flag is present`() {
        assertTrue(CopyCatArgumentCatcher(arrayOf("-gui")).requestsGui())
    }

    @Test
    fun `requestsGui returns false when gui flag is absent`() {
        assertFalse(CopyCatArgumentCatcher(arrayOf("-src", srcPath)).requestsGui())
    }

    // -------------------------------------------------------------------------
    // retrieveSrcDirFromArg
    // -------------------------------------------------------------------------

    @Test
    fun `retrieveSrcDirFromArg returns null when src arg is missing`() {
        assertNull(CopyCatArgumentCatcher(arrayOf()).retrieveSrcDirFromArg())
    }

    @Test
    fun `retrieveSrcDirFromArg returns null when directory does not exist`() {
        assertNull(CopyCatArgumentCatcher(arrayOf("-src", "nonexistent/path")).retrieveSrcDirFromArg())
    }

    @Test
    fun `retrieveSrcDirFromArg returns null when path points to a file`() {
        val file = File(srcPath, "fileA.txt")
        assertNull(CopyCatArgumentCatcher(arrayOf("-src", file.path)).retrieveSrcDirFromArg())
    }

    @Test
    fun `retrieveSrcDirFromArg returns null when directory is empty`() {
        File(srcPath).listFiles()?.forEach { it.delete() }
        assertNull(CopyCatArgumentCatcher(arrayOf("-src", srcPath)).retrieveSrcDirFromArg())
    }

    @Test
    fun `retrieveSrcDirFromArg returns directory when valid and non-empty`() {
        val result = CopyCatArgumentCatcher(arrayOf("-src", srcPath)).retrieveSrcDirFromArg()
        assertEquals(File(srcPath), result)
    }

    // -------------------------------------------------------------------------
    // retrieveDestDirFromArg
    // -------------------------------------------------------------------------

    @Test
    fun `retrieveDestDirFromArg returns null when dest arg is missing`() {
        assertNull(CopyCatArgumentCatcher(arrayOf()).retrieveDestDirFromArg(File(srcPath)))
    }

    @Test
    fun `retrieveDestDirFromArg returns null when directory does not exist`() {
        assertNull(CopyCatArgumentCatcher(arrayOf("-dest", "nonexistent/path")).retrieveDestDirFromArg(File(srcPath)))
    }

    @Test
    fun `retrieveDestDirFromArg returns null when dest equals src`() {
        assertNull(CopyCatArgumentCatcher(arrayOf("-dest", srcPath)).retrieveDestDirFromArg(File(srcPath)))
    }

    @Test
    fun `retrieveDestDirFromArg returns directory when valid and different from src`() {
        val result = CopyCatArgumentCatcher(arrayOf("-dest", destPath)).retrieveDestDirFromArg(File(srcPath))
        assertEquals(File(destPath), result)
    }

    @Test
    fun `retrieveDestDirFromArg returns directory when srcDir is null`() {
        val result = CopyCatArgumentCatcher(arrayOf("-dest", destPath)).retrieveDestDirFromArg(null)
        assertEquals(File(destPath), result)
    }

    // -------------------------------------------------------------------------
    // retrieveCompDirFromArg
    // -------------------------------------------------------------------------

    @Test
    fun `retrieveCompDirFromArg returns null silently when comp arg is missing`() {
        assertNull(CopyCatArgumentCatcher(arrayOf()).retrieveCompDirFromArg(File(srcPath)))
    }

    @Test
    fun `retrieveCompDirFromArg returns null when directory does not exist`() {
        assertNull(CopyCatArgumentCatcher(arrayOf("-comp", "nonexistent/path")).retrieveCompDirFromArg(File(srcPath)))
    }

    @Test
    fun `retrieveCompDirFromArg returns null when comp equals src`() {
        assertNull(CopyCatArgumentCatcher(arrayOf("-comp", srcPath)).retrieveCompDirFromArg(File(srcPath)))
    }

    @Test
    fun `retrieveCompDirFromArg returns directory when valid and different from src`() {
        val result = CopyCatArgumentCatcher(arrayOf("-comp", compPath)).retrieveCompDirFromArg(File(srcPath))
        assertEquals(File(compPath), result)
    }

    // -------------------------------------------------------------------------
    // getConfig
    // -------------------------------------------------------------------------

    @Test
    fun `getConfig returns null when src arg is missing`() {
        assertNull(CopyCatArgumentCatcher(arrayOf("-dest", destPath)).getConfig())
    }

    @Test
    fun `getConfig returns null when dest arg is missing`() {
        assertNull(CopyCatArgumentCatcher(arrayOf("-src", srcPath)).getConfig())
    }

    @Test
    fun `getConfig returns configuration with correct src and dest`() {
        val config = CopyCatArgumentCatcher(arrayOf("-src", srcPath, "-dest", destPath)).getConfig()
        assertNotNull(config)
        assertEquals(File(srcPath), config!!.sourceDir)
        assertEquals(File(destPath), config.copyDestDir)
    }

    @Test
    fun `getConfig uses dest as comparison dir when comp is not provided`() {
        // testSrc has fileA, fileB, fileC — testDest has fileB, fileC → only fileA is missing
        val config = CopyCatArgumentCatcher(arrayOf("-src", srcPath, "-dest", destPath)).getConfig()
        assertNotNull(config)
        val names = config!!.filesSelectedToBeCopied.map { it.name }
        assertEquals(listOf("fileA.txt"), names)
    }

    @Test
    fun `getConfig uses explicit comp dir when provided`() {
        // testSrc has fileA, fileB, fileC — testComp has only fileB → fileA and fileC are missing
        val config = CopyCatArgumentCatcher(arrayOf("-src", srcPath, "-dest", destPath, "-comp", compPath)).getConfig()
        assertNotNull(config)
        val names = config!!.filesSelectedToBeCopied.map { it.name }.sorted()
        assertEquals(listOf("fileA.txt", "fileC.abc"), names)
    }

    @Test
    fun `getConfig filters selected files to requested type`() {
        // Only txt files missing from comp: fileA.txt
        val config = CopyCatArgumentCatcher(
            arrayOf("-src", srcPath, "-dest", destPath, "-comp", compPath, "-types", "txt")
        ).getConfig()
        assertNotNull(config)
        val files = config!!.filesSelectedToBeCopied
        assertTrue(files.isNotEmpty())
        assertTrue(files.all { it.extension == "txt" })
    }

    @Test
    fun `getConfig includes files of all requested types`() {
        // testComp missing fileA.txt (txt) and fileC.abc (abc) — both types requested
        val config = CopyCatArgumentCatcher(
            arrayOf("-src", srcPath, "-dest", destPath, "-comp", compPath, "-types", "txt", "abc")
        ).getConfig()
        assertNotNull(config)
        val extensions = config!!.filesSelectedToBeCopied.map { it.extension }.toSet()
        assertTrue("txt" in extensions)
        assertTrue("abc" in extensions)
    }

    @Test
    fun `getConfig returns empty file list when requested type has no missing files`() {
        // All files in testSrc also exist in testDest except fileA.txt — requesting only abc yields nothing
        val config = CopyCatArgumentCatcher(
            arrayOf("-src", srcPath, "-dest", destPath, "-types", "abc")
        ).getConfig()
        assertNotNull(config)
        assertTrue(config!!.filesSelectedToBeCopied.isEmpty())
    }

    // -------------------------------------------------------------------------
    // parseArgs edge cases (tested indirectly)
    // -------------------------------------------------------------------------

    @Test
    fun `unknown flags are ignored and remaining args are still parsed`() {
        val result = CopyCatArgumentCatcher(arrayOf("-unknown", "value", "-src", srcPath)).retrieveSrcDirFromArg()
        assertEquals(File(srcPath), result)
    }

    @Test
    fun `non-flag tokens before first flag are ignored`() {
        val result = CopyCatArgumentCatcher(arrayOf("garbage", "more", "-src", srcPath)).retrieveSrcDirFromArg()
        assertEquals(File(srcPath), result)
    }
}
