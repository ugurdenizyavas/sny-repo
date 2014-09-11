package com.sony.ebs.octopus3.microservices.reposervice.business

class FileAttributes {

    String urn

    String lastModifiedTime

    String lastAccessTime

    String creationTime

    boolean regularFile

    boolean directory

    long size

    def contentFiles

}
