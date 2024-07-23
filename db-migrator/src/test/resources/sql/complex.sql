-- END

update dav_file set parent = ( select id from ( select id from dav_file where name = '__SITE_PROTECTED__' ) as x )
  where ( name = 'templates' and parent is null )
     or ( name = 'velocity' and parent is null )
     or ( name = 'tags' and parent is null )
     or ( name = 'ctd' and parent is null );
