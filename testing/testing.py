#!/usr/bin/env python2
# -*- coding: utf-8 -*-

import __future__
import sys,os






class testing(object):    
    from unittest import TestCase,TestSuite
    import re
    
    TEST_FORMAT  = 'TEST_FORMAT'
    TEST_TAPFILE = 'TEST_TAPFILE'
    TEST_XMLFILE = 'TEST_XMLFILE'
    
    # list of test formats form TEST_FORMAT env
    test_format  = re.findall(r"[\w']+", os.getenv(TEST_FORMAT,'tap'))
    
    tap_file = os.getenv(TEST_TAPFILE, os.path.splitext(sys.argv[1])[0])
    xml_file = os.getenv(TEST_XMLFILE, os.path.splitext(sys.argv[1])[0])

    def check_module(self, module_name ):
        from modulefinder import ModuleFinder
        finder = self.ModuleFinder(debug=2)
        finder.run_script(module_name)
        for name, mod in finder.modules.items():
            try:                
                __import__(name, fromlist=mod.globalnames.keys(),level=1)
                sys.stdout.write('.')
            except ImportError, e:
                print("ERROR IMPORTING %s: " % name + "  --  "+e.message)
        
    def check_loadmethod(self, file_name, class_name, method_name ):
        import imp
        m = imp.load_source(class_name, file_name)
        getattr(m, method_name)()

    def check_loadlib(self, lib):
        import ctypes
        ctypes.CDLL(lib)
        
    def skip_test(self, module_name, message):
        # TODO: fix this
        if self.test_format == 'tap':
            print("ok 1 - " + module_name + " # SKIP " + message)
            print("1..1")
        sys.exit(77)
        
    def run_tap(self, module):                
        import tap,unittest
        tr = tap.TAPTestRunner()
        tr.set_stream(1)        
        loader = unittest.TestLoader()
        tests = loader.loadTestsFromModule(module)
        tr.run(tests)

    def run_nose(self, module_name):
        import nose
        import shutil
        f = self.test_format
        nose_aux_args = ['-d','-s','-v']
        res = 0
        if len(f) > 1:
            if 'log' in f and 'tap' in f and 'xml' in f:
                res = nose.run(argv=[ sys.argv[1], module_name,
                '--with-tap','--tap-combined','--tap-out=.'+os.path.basename(sys.argv[1]),
                '--tap-format="{method_name} {short_description}"',
                '--with-xunit','--xunit-file='+self.xml_file] + nose_aux_args)
                shutil.copyfile('.'+os.path.basename(sys.argv[1])+'/testresults.tap',self.tap_file)
                shutil.rmtree('.'+os.path.basename(sys.argv[1]))             
                
            elif 'log' in f and 'tap' in f:
                res = nose.run(argv=[ sys.argv[1], module_name,
                '--with-tap','--tap-combined','--tap-out=.'+os.path.basename(sys.argv[1]),
                '--tap-format="{method_name} {short_description}"'] + nose_aux_args)
                shutil.copyfile('.'+os.path.basename(sys.argv[1])+'/testresults.tap',self.tap_file)
                shutil.rmtree('.'+os.path.basename(sys.argv[1]))
            
            elif 'log' in f and 'xml' in f:
                res = nose.run(argv=[ sys.argv[1], module_name,
                '--with-xunit','--xunit-file='+self.xml_file] + nose_aux_args)
        elif len(f) > 0:
            if 'tap' in f:
                res = nose.run(argv=[ sys.argv[1], module_name,
                '--with-tap','--tap-stream',
                '--tap-format="{method_name} {short_description}"'] + nose_aux_args)
                
            elif 'xml' in f:
                print("WARNING: xml output can not be streamed to stdout")
                res = nose.run(argv=[ sys.argv[1], module_name,
                '--with-xunit','--xunit-file='+self.xml_file] + nose_aux_args)
            else:
                res = nose.run(argv=[ sys.argv[1], module_name] + nose_aux_args)                
        return res
        
        


ts = testing()

def check_arch(file_name):
    if sys.platform.startswith('win'):
        lib='MdsShr.dll'
    elif sys.platform.startswith('darwin'):
        lib='libMdsShr.dylib'
    else:
        lib='libMdsShr.so'
    try:
        ts.check_loadlib(lib)
    except OSError:
        ts.skip_test(os.path.basename(file_name),
                     'Unable to load MDSplus core libs')

#def run():
#    import inspect
#    frame = inspect.stack()[1]
#    check_arch(frame[1])
#    try:
#        ts.run_nose(frame[1])
#    except:
#        module = inspect.getmodule(frame[0])
#        try:
#            ts.run(module)
#        except:
#            ts.skip_test(module.__name__,'Unable to run tests')


if __name__ == '__main__':
    import inspect
    if '--skip' in sys.argv or 'ENABLE_SANITIZE' in os.environ:
        ts.skip_test(sys.argv[1],'Skipped tests that was compiled with sanitize options')
    sys.argv[0] = sys.argv[1]
    check_arch(sys.argv[1])
    try:
        res = ts.run_nose(sys.argv[1])
        sys.exit( not res )
    except SystemExit:
        raise
    except:
        ts.skip_test(sys.argv[1],str(sys.exc_info()[0]))

