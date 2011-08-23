;; scratch buffer created 2011-08-04 at 18:27:57

;; (defun void-function (param)
;;   (let* ((class (jclass "board.Main"))
;;          (intclass (jclass "int"))
;;          (method (jmethod class "addTwoNumbers" intclass intclass))
;;          (result (jcall method param 2 4)))
;;     (format t "in void-function, result of calling addTwoNumbers(2, 4): ~a~%" result)))

(defun main ()
  (jstatic "main" "board.Whiteboard" (jnew-array-from-array "java.lang.String" (make-array 0))))

(defun make-whiteboard ()
  (jnew "board.Whiteboard"))

(defun load-rdf (pathname &optional (language "N3"))
  (jstatic "loadRdf" "board.Whiteboard" pathname language))

(defun post-update (board &key (pathname "sample1.n3") (model (load-rdf pathname)))
  (jcall (jmethod (jclass "board.Whiteboard") "postUpdate" (jclass "board.Client") (jclass "com.hp.hpl.jena.rdf.model.Model"))
	 board java:+null+ model))

(defun parse (string)
  (jstatic "create" "com.hp.hpl.jena.query.QueryFactory" string))

(defun rdf-uri ()
  (jstatic "getURI" "com.hp.hpl.jena.vocabulary.RDF"))

(defun load-ontology (pathname &optional (language "RDF/XML"))
  (jstatic "loadOntology" "board.Whiteboard" pathname language))

(defun make-resource (uriref)
  (jstatic "createResource" "com.hp.hpl.jena.rdf.model.ResourceFactory" uriref))

(defun get-local-name (resource)
  (jcall (jmethod "com.hp.hpl.jena.rdf.model.Resource" "getLocalName") resource))
